package database;

import models.*;
import java.util.*;
import java.util.stream.Collectors;

public class TimetableGenerator {

    private final TeacherDAO          teacherDAO    = new TeacherDAO();
    private final ClassroomDAO        classroomDAO  = new ClassroomDAO();
    private final TimeSlotDAO         slotDAO       = new TimeSlotDAO();
    private final SubjectAssignmentDAO assignmentDAO = new SubjectAssignmentDAO();
    private final TimetableDAO        timetableDAO  = new TimetableDAO();

    // ── Conflict Record ───────────────────────────────────────────────────────
    public static class ConflictRecord {
        public final String subjectName;
        public final String section;
        public final String teacherName;
        public final String reason;
        public final String suggestion;

        public ConflictRecord(String subjectName, String section, String teacherName,
                              String reason, String suggestion) {
            this.subjectName = subjectName;
            this.section     = section;
            this.teacherName = teacherName;
            this.reason      = reason;
            this.suggestion  = suggestion;
        }
    }

    // ── Generation Result ─────────────────────────────────────────────────────
    public static class GenerationResult {
        public int successCount;
        public int failCount;
        public int swapResolutions;
        public final List<ConflictRecord> conflicts   = new ArrayList<>();
        public final List<String>         logs        = new ArrayList<>();
        public final List<String>         preWarnings = new ArrayList<>();

        public List<String> toLogLines() {
            List<String> all = new ArrayList<>();
            if (!preWarnings.isEmpty()) {
                all.add("── Pre-Generation Analysis ──────────────────────────────");
                all.addAll(preWarnings);
                all.add("");
            }
            all.addAll(logs);
            if (!conflicts.isEmpty()) {
                all.add("");
                all.add("── Conflict Report ──────────────────────────────────────");
                for (ConflictRecord c : conflicts) {
                    all.add("✗ " + c.subjectName + "  [" + c.section + "]");
                    all.add("  Teacher   : " + c.teacherName);
                    all.add("  Reason    : " + c.reason);
                    if (c.suggestion != null && !c.suggestion.isEmpty())
                        all.add("  Suggestion: " + c.suggestion);
                    all.add("");
                }
            }
            all.add("─────────────────────────────────────────────────────────");
            all.add("Scheduled : " + successCount);
            if (swapResolutions > 0)
                all.add("Resolved via swap : " + swapResolutions);
            all.add("Conflicts : " + failCount);
            return all;
        }
    }

    // ── In-memory placement entry ─────────────────────────────────────────────
    private static class PlacedEntry {
        final SubjectAssignment sa;
        final int slotId;
        int roomId;
        final String day;

        PlacedEntry(SubjectAssignment sa, int slotId, int roomId, String day) {
            this.sa = sa; this.slotId = slotId; this.roomId = roomId; this.day = day;
        }
    }

    // ── Main entry point ──────────────────────────────────────────────────────
    public GenerationResult generate() {
        GenerationResult result = new GenerationResult();

        List<SubjectAssignment> assignments = assignmentDAO.getAll();
        List<TimeSlot>          slots       = slotDAO.getAll();
        List<Classroom>         rooms       = classroomDAO.getAll();
        List<Teacher>           teachers    = teacherDAO.getAll();

        if (assignments.isEmpty()) { result.logs.add("Error: No subject assignments found."); return result; }
        if (slots.isEmpty())       { result.logs.add("Error: No time slots defined.");        return result; }
        if (rooms.isEmpty())       { result.logs.add("Error: No classrooms registered.");     return result; }

        Map<Integer, Teacher> teacherMap = buildTeacherMap(teachers);

        // ── Pre-generation analysis ──────────────────────────────────────────
        preAnalyze(assignments, slots, teachers, result);

        // ── Multi-trial scheduling (3 trials, pick best) ─────────────────────
        List<PlacedEntry>         bestPlaced      = null;
        List<SubjectAssignment>   bestUnscheduled = null;

        for (int trial = 0; trial < 3; trial++) {
            List<SubjectAssignment> shuffled     = new ArrayList<>(assignments);
            Collections.shuffle(shuffled);
            List<SubjectAssignment> unscheduled  = new ArrayList<>();
            List<PlacedEntry>       placed       = runTrial(shuffled, slots, rooms, teacherMap, unscheduled);

            if (bestPlaced == null || placed.size() > bestPlaced.size()) {
                bestPlaced      = placed;
                bestUnscheduled = new ArrayList<>(unscheduled);
            }
            if (bestUnscheduled.isEmpty()) break;
        }

        result.logs.add("Best trial: " + bestPlaced.size() + " scheduled, " + bestUnscheduled.size() + " pending.");

        // ── Swap-based conflict resolution ───────────────────────────────────
        if (!bestUnscheduled.isEmpty()) {
            result.logs.add("Attempting swap resolution for " + bestUnscheduled.size() + " item(s)…");
            int resolved = trySwapResolution(bestUnscheduled, bestPlaced, slots, rooms, teacherMap);
            result.swapResolutions = resolved;
            if (resolved > 0)
                result.logs.add("Swap resolution freed " + resolved + " additional slot(s).");
        }

        // ── Write best result to DB ──────────────────────────────────────────
        DatabaseManager.getInstance().clearTimetable();
        for (PlacedEntry pe : bestPlaced) {
            timetableDAO.insert(new TimetableEntry(
                0, pe.day, pe.slotId,
                pe.sa.getSubjectId(), pe.sa.getTeacherId(), pe.roomId));
        }
        result.successCount = bestPlaced.size();

        // ── Build conflict records for remaining unscheduled ─────────────────
        Map<Integer, Set<Integer>> teacherBookings  = buildTeacherBookings(bestPlaced);
        Map<Integer, Set<String>>  sectionBookings  = buildSectionBookings(bestPlaced);
        Map<Integer, Set<Integer>> roomBookings     = buildRoomBookings(bestPlaced);

        for (SubjectAssignment sa : bestUnscheduled) {
            Teacher t      = teacherMap.get(sa.getTeacherId());
            String reason  = diagnoseConflict(sa, slots, rooms, teacherMap,
                                              teacherBookings, sectionBookings, roomBookings);
            String suggest = buildSuggestion(sa, slots, t, assignments);
            result.conflicts.add(new ConflictRecord(
                sa.getSubjectName(), sa.getSubjectSection(),
                t != null ? t.getName() : "Unknown", reason, suggest));
            result.failCount++;
        }

        result.logs.add("Generation complete.");
        return result;
    }

    // ── Single trial (in-memory, no DB write) ────────────────────────────────
    private List<PlacedEntry> runTrial(List<SubjectAssignment> assignments,
            List<TimeSlot> slots, List<Classroom> rooms,
            Map<Integer, Teacher> teacherMap,
            List<SubjectAssignment> unscheduled) {

        Map<Integer, Set<Integer>> teacherBookings  = new HashMap<>();
        Map<Integer, Set<Integer>> roomBookings     = new HashMap<>();
        Map<Integer, Set<String>>  sectionBookings  = new HashMap<>();
        Map<String,  Set<String>>  sectionActiveDays = new HashMap<>();
        List<PlacedEntry>          placed           = new ArrayList<>();

        for (SubjectAssignment sa : assignments) {
            String  section = sa.getSubjectSection();
            Teacher teacher = teacherMap.get(sa.getTeacherId());
            sectionActiveDays.putIfAbsent(section, new HashSet<>());

            // Prefer slots on days the section hasn't used yet
            Set<String> usedDays = sectionActiveDays.get(section);
            List<TimeSlot> prioritized = slots.stream()
                .sorted(Comparator.comparingInt(s -> usedDays.contains(s.getDay()) ? 1 : 0))
                .collect(Collectors.toList());

            boolean scheduled = false;
            outer:
            for (TimeSlot slot : prioritized) {
                if (teacher != null && !isTeacherAvailable(teacher.getAvailability(), slot.getDay())) continue;
                teacherBookings.putIfAbsent(slot.getId(), new HashSet<>());
                if (teacherBookings.get(slot.getId()).contains(sa.getTeacherId())) continue;
                sectionBookings.putIfAbsent(slot.getId(), new HashSet<>());
                if (sectionBookings.get(slot.getId()).contains(section)) continue;

                roomBookings.putIfAbsent(slot.getId(), new HashSet<>());
                for (Classroom room : rooms) {
                    if (roomBookings.get(slot.getId()).contains(room.getId())) continue;
                    placed.add(new PlacedEntry(sa, slot.getId(), room.getId(), slot.getDay()));
                    teacherBookings.get(slot.getId()).add(sa.getTeacherId());
                    roomBookings.get(slot.getId()).add(room.getId());
                    sectionBookings.get(slot.getId()).add(section);
                    sectionActiveDays.get(section).add(slot.getDay());
                    scheduled = true;
                    break outer;
                }
            }
            if (!scheduled) unscheduled.add(sa);
        }
        return placed;
    }

    // ── Swap-based resolution: move a placed entry to free a slot ────────────
    private int trySwapResolution(List<SubjectAssignment> unscheduled,
            List<PlacedEntry> placed, List<TimeSlot> slots, List<Classroom> rooms,
            Map<Integer, Teacher> teacherMap) {

        // Rebuild live booking maps from placed list
        Map<Integer, Set<Integer>> teacherBookings  = buildTeacherBookings(placed);
        Map<Integer, Set<String>>  sectionBookings  = buildSectionBookings(placed);
        Map<Integer, Set<Integer>> roomBookings     = buildRoomBookings(placed);

        List<SubjectAssignment> resolved = new ArrayList<>();
        int swapCount = 0;

        for (SubjectAssignment sa : unscheduled) {
            Teacher saTeacher = teacherMap.get(sa.getTeacherId());

            for (TimeSlot slot : slots) {
                if (saTeacher != null && !isTeacherAvailable(saTeacher.getAvailability(), slot.getDay())) continue;
                teacherBookings.putIfAbsent(slot.getId(), new HashSet<>());
                if (teacherBookings.get(slot.getId()).contains(sa.getTeacherId())) continue;

                sectionBookings.putIfAbsent(slot.getId(), new HashSet<>());
                if (!sectionBookings.get(slot.getId()).contains(sa.getSubjectSection())) {
                    // Slot is fine for section but maybe no room — skip (trySchedule handles this)
                    continue;
                }

                // Section is blocked — find the blocker placed entry
                PlacedEntry blocker = placed.stream()
                    .filter(pe -> pe.slotId == slot.getId()
                                  && pe.sa.getSubjectSection().equals(sa.getSubjectSection())
                                  && pe.sa.getTeacherId() != sa.getTeacherId())
                    .findFirst().orElse(null);
                if (blocker == null) continue;

                // Try to move the blocker to an alternative slot
                Teacher blockerTeacher = teacherMap.get(blocker.sa.getTeacherId());
                for (TimeSlot altSlot : slots) {
                    if (altSlot.getId() == slot.getId()) continue;
                    if (blockerTeacher != null && !isTeacherAvailable(blockerTeacher.getAvailability(), altSlot.getDay())) continue;
                    teacherBookings.putIfAbsent(altSlot.getId(), new HashSet<>());
                    if (teacherBookings.get(altSlot.getId()).contains(blocker.sa.getTeacherId())) continue;
                    sectionBookings.putIfAbsent(altSlot.getId(), new HashSet<>());
                    if (sectionBookings.get(altSlot.getId()).contains(blocker.sa.getSubjectSection())) continue;

                    roomBookings.putIfAbsent(altSlot.getId(), new HashSet<>());
                    Classroom altRoom = rooms.stream()
                        .filter(r -> !roomBookings.get(altSlot.getId()).contains(r.getId()))
                        .findFirst().orElse(null);
                    if (altRoom == null) continue;

                    // Find a room for sa in the now-freed slot
                    roomBookings.get(slot.getId()).remove(blocker.roomId);
                    sectionBookings.get(slot.getId()).remove(blocker.sa.getSubjectSection());
                    teacherBookings.get(slot.getId()).remove(blocker.sa.getTeacherId());

                    Classroom saRoom = rooms.stream()
                        .filter(r -> !roomBookings.get(slot.getId()).contains(r.getId()))
                        .findFirst().orElse(null);

                    if (saRoom == null) {
                        // Undo release
                        roomBookings.get(slot.getId()).add(blocker.roomId);
                        sectionBookings.get(slot.getId()).add(blocker.sa.getSubjectSection());
                        teacherBookings.get(slot.getId()).add(blocker.sa.getTeacherId());
                        continue;
                    }

                    // Commit swap
                    // Move blocker → altSlot
                    teacherBookings.get(altSlot.getId()).add(blocker.sa.getTeacherId());
                    roomBookings.get(altSlot.getId()).add(altRoom.getId());
                    sectionBookings.get(altSlot.getId()).add(blocker.sa.getSubjectSection());
                    placed.remove(blocker);
                    placed.add(new PlacedEntry(blocker.sa, altSlot.getId(), altRoom.getId(), altSlot.getDay()));

                    // Place sa → slot
                    teacherBookings.get(slot.getId()).add(sa.getTeacherId());
                    roomBookings.get(slot.getId()).add(saRoom.getId());
                    sectionBookings.get(slot.getId()).add(sa.getSubjectSection());
                    placed.add(new PlacedEntry(sa, slot.getId(), saRoom.getId(), slot.getDay()));

                    resolved.add(sa);
                    swapCount++;
                    break;
                }
                if (resolved.contains(sa)) break;
            }
        }
        unscheduled.removeAll(resolved);
        return swapCount;
    }

    // ── Pre-generation analysis ───────────────────────────────────────────────
    private void preAnalyze(List<SubjectAssignment> assignments, List<TimeSlot> slots,
                             List<Teacher> teachers, GenerationResult result) {
        Map<Integer, Long> courseCount = assignments.stream()
            .collect(Collectors.groupingBy(SubjectAssignment::getTeacherId, Collectors.counting()));
        Map<Integer, Teacher> teacherMap = buildTeacherMap(teachers);

        for (Map.Entry<Integer, Long> e : courseCount.entrySet()) {
            Teacher t = teacherMap.get(e.getKey());
            if (t == null) continue;
            long available = countAvailableSlots(t.getAvailability(), slots);
            long needed    = e.getValue();
            if (needed > available) {
                result.preWarnings.add(
                    "⚠  " + t.getName() + " → " + needed + " course(s) assigned, only "
                    + available + " slot(s) available on their days. Expect "
                    + (needed - available) + " unscheduled class(es).");
            } else {
                result.preWarnings.add(
                    "✓  " + t.getName() + " → " + needed + " course(s), " + available + " available slots (OK).");
            }
        }
    }

    // ── Conflict Diagnosis ───────────────────────────────────────────────────
    private String diagnoseConflict(SubjectAssignment sa, List<TimeSlot> slots, List<Classroom> rooms,
            Map<Integer, Teacher> teacherMap,
            Map<Integer, Set<Integer>> teacherBookings,
            Map<Integer, Set<String>>  sectionBookings,
            Map<Integer, Set<Integer>> roomBookings) {

        Teacher t = teacherMap.get(sa.getTeacherId());
        if (t == null) return "Teacher record not found in system.";

        boolean hasAvailableDay      = false;
        boolean teacherOverloaded    = true;
        boolean sectionFullyBooked   = true;
        boolean noRoomInAnySlot      = true;

        for (TimeSlot slot : slots) {
            if (!isTeacherAvailable(t.getAvailability(), slot.getDay())) continue;
            hasAvailableDay = true;

            boolean tBooked = teacherBookings.getOrDefault(slot.getId(), Collections.emptySet())
                                             .contains(sa.getTeacherId());
            boolean sBooked = sectionBookings.getOrDefault(slot.getId(), Collections.emptySet())
                                             .contains(sa.getSubjectSection());

            if (!tBooked)  teacherOverloaded  = false;
            if (!sBooked)  sectionFullyBooked = false;

            Set<Integer> bookedRooms = roomBookings.getOrDefault(slot.getId(), Collections.emptySet());
            if (bookedRooms.size() < rooms.size()) noRoomInAnySlot = false;
        }

        if (!hasAvailableDay)
            return "Teacher '" + t.getName() + "' has no matching day in availability ('"
                   + t.getAvailability() + "'). No slot can be assigned.";
        if (teacherOverloaded)
            return "Teacher '" + t.getName() + "' is already scheduled in every available slot — course overload.";
        if (sectionFullyBooked)
            return "Section '" + sa.getSubjectSection() + "' has no free slot on teacher's available days — section is overloaded.";
        if (noRoomInAnySlot)
            return "All classrooms are fully occupied during all valid slots — add more classrooms.";
        return "Combined constraint deadlock: teacher + section + room constraints leave no valid slot.";
    }

    // ── Suggestion builder ───────────────────────────────────────────────────
    private String buildSuggestion(SubjectAssignment sa, List<TimeSlot> slots, Teacher t,
                                   List<SubjectAssignment> allAssignments) {
        if (t == null) return "Verify that the teacher record exists.";
        long available = countAvailableSlots(t.getAvailability(), slots);
        if (available == 0)
            return "Extend '" + t.getName() + "' availability to at least one working day.";

        long assigned = allAssignments.stream()
            .filter(a -> a.getTeacherId() == t.getId()).count();
        if (assigned > available)
            return t.getName() + " has " + assigned + " course(s) but only " + available
                   + " slot(s). Reduce course load OR extend availability.";

        return "Re-run generation — probabilistic reordering may resolve this scheduling conflict.";
    }

    // ── Public: conflict analysis from current DB state (no regeneration) ────
    public GenerationResult analyzeCurrentState() {
        GenerationResult result = new GenerationResult();
        List<SubjectAssignment> assignments = assignmentDAO.getAll();
        List<TimeSlot>          slots       = slotDAO.getAll();
        List<Classroom>         rooms       = classroomDAO.getAll();
        List<Teacher>           teachers    = teacherDAO.getAll();
        Map<Integer, Teacher>   teacherMap  = buildTeacherMap(teachers);

        preAnalyze(assignments, slots, teachers, result);

        List<TimetableEntry> scheduled = timetableDAO.getAll();
        Set<Integer> scheduledSubjectIds = scheduled.stream()
            .map(TimetableEntry::getSubjectId).collect(Collectors.toSet());

        List<SubjectAssignment> unscheduled = assignments.stream()
            .filter(sa -> !scheduledSubjectIds.contains(sa.getSubjectId()))
            .collect(Collectors.toList());

        result.successCount = scheduled.size();
        result.failCount    = unscheduled.size();

        // Build booking maps from current timetable
        Map<Integer, Set<Integer>> teacherBookings = new HashMap<>();
        Map<Integer, Set<String>>  sectionBookings = new HashMap<>();
        Map<Integer, Set<Integer>> roomBookings    = new HashMap<>();
        for (TimetableEntry e : scheduled) {
            teacherBookings.computeIfAbsent(e.getTimeSlotId(), k -> new HashSet<>()).add(e.getTeacherId());
            roomBookings.computeIfAbsent(e.getTimeSlotId(), k -> new HashSet<>()).add(e.getClassroomId());
            // Section tracking from section name on the entry
            if (e.getSectionName() != null)
                sectionBookings.computeIfAbsent(e.getTimeSlotId(), k -> new HashSet<>()).add(e.getSectionName());
        }

        for (SubjectAssignment sa : unscheduled) {
            Teacher t = teacherMap.get(sa.getTeacherId());
            String reason  = diagnoseConflict(sa, slots, rooms, teacherMap,
                                              teacherBookings, sectionBookings, roomBookings);
            String suggest = buildSuggestion(sa, slots, t, assignments);
            result.conflicts.add(new ConflictRecord(
                sa.getSubjectName(), sa.getSubjectSection(),
                t != null ? t.getName() : "Unknown", reason, suggest));
        }
        result.logs.add("Analysis complete.");
        return result;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    public boolean isTeacherAvailable(String avail, String day) {
        if (avail == null || avail.isBlank()
            || avail.equalsIgnoreCase("All")
            || avail.equalsIgnoreCase("Available")
            || avail.equalsIgnoreCase("All Days")) return true;
        String la = avail.toLowerCase(), ld = day.toLowerCase();
        return la.contains(ld) || (ld.length() >= 3 && la.contains(ld.substring(0, 3)));
    }

    public String getTeacherAvailability(int id) {
        return teacherDAO.getAll().stream()
            .filter(t -> t.getId() == id)
            .map(Teacher::getAvailability)
            .findFirst().orElse("");
    }

    private long countAvailableSlots(String avail, List<TimeSlot> slots) {
        return slots.stream().filter(s -> isTeacherAvailable(avail, s.getDay())).count();
    }

    private Map<Integer, Teacher> buildTeacherMap(List<Teacher> teachers) {
        Map<Integer, Teacher> m = new HashMap<>();
        for (Teacher t : teachers) m.put(t.getId(), t);
        return m;
    }

    private Map<Integer, Set<Integer>> buildTeacherBookings(List<PlacedEntry> placed) {
        Map<Integer, Set<Integer>> m = new HashMap<>();
        for (PlacedEntry pe : placed)
            m.computeIfAbsent(pe.slotId, k -> new HashSet<>()).add(pe.sa.getTeacherId());
        return m;
    }

    private Map<Integer, Set<String>> buildSectionBookings(List<PlacedEntry> placed) {
        Map<Integer, Set<String>> m = new HashMap<>();
        for (PlacedEntry pe : placed)
            m.computeIfAbsent(pe.slotId, k -> new HashSet<>()).add(pe.sa.getSubjectSection());
        return m;
    }

    private Map<Integer, Set<Integer>> buildRoomBookings(List<PlacedEntry> placed) {
        Map<Integer, Set<Integer>> m = new HashMap<>();
        for (PlacedEntry pe : placed)
            m.computeIfAbsent(pe.slotId, k -> new HashSet<>()).add(pe.roomId);
        return m;
    }
}
