package gui;

import com.formdev.flatlaf.FlatClientProperties;
import database.TimetableDAO;
import models.TimetableEntry;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.*;
import java.util.List;

public class ViewTimetablePanel extends JPanel {

    private final TimetableDAO       dao        = new TimetableDAO();
    private final JTable             table;
    private final DefaultTableModel  model;
    private static final String[]    DAYS       =
        {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

    private final JComboBox<String> filterType  =
        new JComboBox<>(new String[]{"By Section", "By Teacher", "By Classroom", "By Department", "By Day", "Teacher Workload"});
    private final JComboBox<Object> filterValue = new JComboBox<>();
    private final JButton printBtn = new JButton("🖨  Export / Print");
    private final JButton csvBtn   = new JButton("📊  Export CSV");

    public ViewTimetablePanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(StyleConstants.BG_DARK);

        // ── Top bar ───────────────────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(StyleConstants.BG_CARD);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(StyleConstants.BORDER);
                g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
                g2.dispose();
            }
        };
        topBar.setOpaque(false);
        topBar.setPreferredSize(new Dimension(0, 62));
        topBar.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 24));

        JLabel titleLbl = new JLabel("COMSATS VEHARI — CENTRALIZED TIMETABLE");
        titleLbl.setFont(new Font("Inter", Font.BOLD, 16));
        titleLbl.setForeground(StyleConstants.ACCENT);
        topBar.add(titleLbl, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        // Style CSV button
        csvBtn.setFont(new Font("Inter", Font.BOLD, 12));
        csvBtn.setForeground(StyleConstants.TEXT_PRIMARY);
        csvBtn.setBackground(StyleConstants.BG_SURFACE);
        csvBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        csvBtn.putClientProperty(FlatClientProperties.STYLE, "arc: 10");

        // Style Print button
        printBtn.setFont(new Font("Inter", Font.BOLD, 12));
        printBtn.setBackground(StyleConstants.ACCENT);
        printBtn.setForeground(Color.WHITE);
        printBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        printBtn.putClientProperty(FlatClientProperties.STYLE, "arc: 10");

        actions.add(csvBtn);
        actions.add(printBtn);
        topBar.add(actions, BorderLayout.EAST);

        // ── Filter bar ────────────────────────────────────────────────────
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 10));
        filterBar.setBackground(StyleConstants.BG_DARK);
        filterBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, StyleConstants.BORDER));

        JLabel groupByLbl = new JLabel("GROUP BY");
        groupByLbl.setFont(StyleConstants.FONT_LABEL);
        groupByLbl.setForeground(StyleConstants.TEXT_MUTED);

        filterType.setFont(StyleConstants.FONT_BODY);
        filterType.setPreferredSize(new Dimension(160, 34));

        JLabel selectLbl = new JLabel("FILTER");
        selectLbl.setFont(StyleConstants.FONT_LABEL);
        selectLbl.setForeground(StyleConstants.TEXT_MUTED);

        filterValue.setFont(StyleConstants.FONT_BODY);
        filterValue.setPreferredSize(new Dimension(240, 34));

        JButton refreshBtn = new JButton("↻  Apply");
        refreshBtn.setFont(new Font("Inter", Font.BOLD, 12));
        refreshBtn.setBackground(StyleConstants.ACCENT);
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refreshBtn.putClientProperty(FlatClientProperties.STYLE, "arc: 10");
        refreshBtn.addActionListener(e -> refresh());

        filterBar.add(groupByLbl);
        filterBar.add(filterType);
        filterBar.add(selectLbl);
        filterBar.add(filterValue);
        filterBar.add(refreshBtn);

        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.add(topBar,    BorderLayout.NORTH);
        north.add(filterBar, BorderLayout.SOUTH);
        add(north, BorderLayout.NORTH);

        // ── Timetable grid ────────────────────────────────────────────────
        model = new DefaultTableModel() {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(115);
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setShowGrid(true);
        table.setGridColor(StyleConstants.BORDER);

        // Header styling
        table.getTableHeader().setBackground(StyleConstants.BG_CARD);
        table.getTableHeader().setForeground(StyleConstants.TEXT_PRIMARY);
        table.getTableHeader().setFont(new Font("Inter", Font.BOLD, 13));
        table.getTableHeader().setPreferredSize(new Dimension(0, 42));
        table.getTableHeader().setReorderingAllowed(false);

        // Cell renderer: color-coded by subject
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {

                JPanel cell = new JPanel(new BorderLayout(0, 4));
                cell.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
                cell.setOpaque(true);

                // Day column
                if (col == 0) {
                    cell.setBackground(StyleConstants.BG_CARD);
                    JLabel dayLbl = new JLabel(value == null ? "" : value.toString().toUpperCase());
                    dayLbl.setFont(new Font("Inter", Font.BOLD, 13));
                    dayLbl.setForeground(StyleConstants.ACCENT);
                    dayLbl.setHorizontalAlignment(SwingConstants.CENTER);
                    cell.add(dayLbl, BorderLayout.CENTER);
                    if (isSelected) cell.setBackground(StyleConstants.BG_SURFACE);
                    return cell;
                }

                // Empty slot
                if (value == null || "---".equals(value.toString()) || value.toString().isEmpty()) {
                    cell.setBackground(StyleConstants.BG_DARK);
                    JLabel dash = new JLabel("—");
                    dash.setForeground(new Color(StyleConstants.TEXT_MUTED.getRed(),
                        StyleConstants.TEXT_MUTED.getGreen(), StyleConstants.TEXT_MUTED.getBlue(), 60));
                    dash.setHorizontalAlignment(SwingConstants.CENTER);
                    cell.add(dash, BorderLayout.CENTER);
                    return cell;
                }

                // Occupied slot: parse "Subject\nTeacher\nRoom"
                String[] parts = value.toString().split("\n");
                String subjectName = parts.length > 0 ? parts[0] : "";

                Color cellBg     = StyleConstants.getCellBg(subjectName);
                Color cellAccent = StyleConstants.getCellAccent(subjectName);

                cell.setBackground(new Color(
                    StyleConstants.BG_CARD.getRed(),
                    StyleConstants.BG_CARD.getGreen(),
                    StyleConstants.BG_CARD.getBlue()));

                // Paint left accent bar + tinted background
                JPanel inner = new JPanel(new BorderLayout()) {
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setColor(cellBg);
                        g2.fillRect(0, 0, getWidth(), getHeight());
                        g2.setColor(cellAccent);
                        g2.fillRect(0, 0, 4, getHeight());
                        g2.dispose();
                    }
                };
                inner.setOpaque(false);
                inner.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 6));
                inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));

                JLabel subLbl = new JLabel(subjectName);
                subLbl.setFont(new Font("Inter", Font.BOLD, 13));
                subLbl.setForeground(StyleConstants.TEXT_PRIMARY);

                JLabel teachLbl = new JLabel(parts.length > 1 ? parts[1] : "");
                teachLbl.setFont(StyleConstants.FONT_SMALL);
                teachLbl.setForeground(StyleConstants.TEXT_SECONDARY);

                JLabel roomLbl = new JLabel(parts.length > 2 ? "🏛 " + parts[2] : "");
                roomLbl.setFont(new Font("Inter", Font.BOLD, 12));
                roomLbl.setForeground(cellAccent);

                inner.add(subLbl);
                inner.add(Box.createVerticalStrut(3));
                inner.add(teachLbl);
                inner.add(Box.createVerticalGlue());
                inner.add(roomLbl);

                cell.setBackground(StyleConstants.BG_CARD);
                cell.add(inner, BorderLayout.CENTER);

                if (isSelected) {
                    cell.setBackground(new Color(
                        StyleConstants.ACCENT.getRed(),
                        StyleConstants.ACCENT.getGreen(),
                        StyleConstants.ACCENT.getBlue(), 40));
                }
                return cell;
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(StyleConstants.BG_DARK);
        add(sp, BorderLayout.CENTER);

        // Wire listeners
        filterType.addActionListener(e -> updateFilterValues());
        printBtn.addActionListener(e -> handleExportPDF());
        csvBtn.addActionListener(e -> exportCSV());

        updateFilterValues();
        refresh();
    }

    // ── Public API ────────────────────────────────────────────────────────

    public void setSectionFilter(String section) {
        filterType.setSelectedItem("By Section");
        filterValue.setSelectedItem(section);
        refresh();
        filterType.setEnabled(false);
        filterValue.setEnabled(false);
    }

    // ── Internal logic ────────────────────────────────────────────────────

    private void updateFilterValues() {
        filterValue.removeAllItems();
        String type = (String) filterType.getSelectedItem();
        if ("By Teacher".equals(type)) {
            for (models.Teacher t : new database.TeacherDAO().getAll()) filterValue.addItem(t);
        } else if ("By Classroom".equals(type)) {
            for (models.Classroom c : new database.ClassroomDAO().getAll()) filterValue.addItem(c);
        } else if ("By Department".equals(type)) {
            Set<String> depts = new LinkedHashSet<>();
            for (models.Subject s : new database.SubjectDAO().getAll()) depts.add(s.getDepartment());
            for (String d : depts) filterValue.addItem(d);
        } else if ("By Day".equals(type)) {
            for (String d : DAYS) filterValue.addItem(d);
        } else if ("Teacher Workload".equals(type)) {
            filterValue.addItem("All Teachers");
        } else { // By Section
            Set<String> sects = new LinkedHashSet<>();
            for (models.Subject s : new database.SubjectDAO().getAll())
                if (s.getSection() != null) sects.add(s.getSection());
            for (String s : sects) filterValue.addItem(s);
        }
    }

    public void refresh() {
        String type = (String) filterType.getSelectedItem();
        Object val  = filterValue.getSelectedItem();

        if ("Teacher Workload".equals(type)) {
            showTeacherWorkload();
            return;
        }
        if ("By Day".equals(type) && val != null) {
            showDayView(val.toString());
            return;
        }

        List<TimetableEntry> entries;
        if (val == null) {
            entries = dao.getAll();
        } else {
            String filterKey = "department";
            Object filterVal = val;
            if ("By Teacher".equals(type))       { filterKey = "teacher";   filterVal = ((models.Teacher)val).getId(); }
            else if ("By Classroom".equals(type)) { filterKey = "classroom"; filterVal = ((models.Classroom)val).getId(); }
            else if ("By Section".equals(type))   { filterKey = "section";   filterVal = val.toString(); }
            entries = dao.getFilteredEntries(filterKey, filterVal);
        }

        // Build column list from time slots
        List<models.TimeSlot> allSlots = new database.TimeSlotDAO().getAll();
        Set<String> timeSet = new TreeSet<>();
        for (models.TimeSlot s : allSlots) timeSet.add(s.getStartTime() + " - " + s.getEndTime());
        List<String> times = new ArrayList<>(timeSet);

        String[] cols = new String[times.size() + 1];
        cols[0] = "Day / Time";
        for (int i = 0; i < times.size(); i++) cols[i + 1] = times.get(i);
        model.setDataVector(new Object[0][cols.length], cols);

        // Map: day → (timeRange → entry)
        Map<String, Map<String, TimetableEntry>> grid = new HashMap<>();
        for (TimetableEntry e : entries)
            grid.computeIfAbsent(e.getDay(), k -> new HashMap<>()).put(e.getTimeRange(), e);

        for (String day : DAYS) {
            Object[] row = new Object[cols.length];
            row[0] = day;
            Map<String, TimetableEntry> dayMap = grid.getOrDefault(day, new HashMap<>());
            for (int i = 0; i < times.size(); i++) {
                TimetableEntry e = dayMap.get(times.get(i));
                row[i + 1] = (e != null)
                    ? e.getSubjectName() + "\n" + e.getTeacherName() + "\n" + e.getClassroomName()
                    : "---";
            }
            model.addRow(row);
        }
    }

    // ── By Day view: show all sections scheduled on a given day ──────────────
    private void showDayView(String day) {
        List<TimetableEntry> entries = dao.getFilteredEntries("day_only", day);
        if (entries.isEmpty()) entries = dao.getAll().stream()
            .filter(e -> day.equals(e.getDay())).collect(java.util.stream.Collectors.toList());

        List<models.TimeSlot> allSlots = new database.TimeSlotDAO().getAll();
        Set<String> timeSet = new TreeSet<>();
        for (models.TimeSlot s : allSlots) timeSet.add(s.getStartTime() + " - " + s.getEndTime());
        List<String> times = new ArrayList<>(timeSet);

        // Columns: TimeSlot | Section-1 | Section-2 | ...
        Set<String> sections = new java.util.LinkedHashSet<>();
        for (TimetableEntry e : entries) if (e.getSectionName() != null) sections.add(e.getSectionName());

        List<String> secList = new ArrayList<>(sections);
        String[] cols = new String[secList.size() + 1];
        cols[0] = day + " — Time Slot";
        for (int i = 0; i < secList.size(); i++) cols[i + 1] = secList.get(i);
        model.setDataVector(new Object[0][cols.length], cols);

        // Map: timeRange → (section → entry)
        Map<String, Map<String, TimetableEntry>> grid = new HashMap<>();
        for (TimetableEntry e : entries) {
            String sec = e.getSectionName() != null ? e.getSectionName() : "—";
            grid.computeIfAbsent(e.getTimeRange(), k -> new HashMap<>()).put(sec, e);
        }

        for (String time : times) {
            Object[] row = new Object[cols.length];
            row[0] = time;
            Map<String, TimetableEntry> slotMap = grid.getOrDefault(time, new HashMap<>());
            for (int i = 0; i < secList.size(); i++) {
                TimetableEntry e = slotMap.get(secList.get(i));
                row[i + 1] = (e != null)
                    ? e.getSubjectName() + "\n" + e.getTeacherName() + "\n" + e.getClassroomName()
                    : "---";
            }
            model.addRow(row);
        }
    }

    // ── Teacher Workload view: classes per teacher per day ───────────────────
    private void showTeacherWorkload() {
        List<TimetableEntry>  all      = dao.getAll();
        List<models.Teacher>  teachers = new database.TeacherDAO().getAll();

        String[] workDays = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
        String[] cols = {"Teacher", "Mon", "Tue", "Wed", "Thu", "Fri", "Total"};
        model.setDataVector(new Object[0][cols.length], cols);

        for (models.Teacher t : teachers) {
            Object[] row = new Object[cols.length];
            row[0] = t.getName();
            int total = 0;
            for (int i = 0; i < workDays.length; i++) {
                final String d = workDays[i];
                long count = all.stream()
                    .filter(e -> e.getTeacherId() == t.getId() && d.equals(e.getDay()))
                    .count();
                row[i + 1] = count == 0 ? "—" : count + " class" + (count > 1 ? "es" : "");
                total += count;
            }
            row[6] = total == 0 ? "0" : total + " total";
            model.addRow(row);
        }

        // Resize rows for workload view
        table.setRowHeight(36);
    }

    private void handleExportPDF() {
        try {
            java.text.MessageFormat hdr = new java.text.MessageFormat(
                "Centralized Timetable — " + filterValue.getSelectedItem());
            java.text.MessageFormat ftr = new java.text.MessageFormat("Page {0}");
            table.print(JTable.PrintMode.FIT_WIDTH, hdr, ftr);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Print Error: " + ex.getMessage());
        }
    }

    private void exportCSV() {
        try {
            List<models.Classroom>     rooms      = new database.ClassroomDAO().getAll();
            List<models.TimeSlot>      allSlots   = new database.TimeSlotDAO().getAll();
            List<models.TimetableEntry> allEntries = dao.getAll();

            if (rooms.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No classrooms found.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            java.io.File file = new java.io.File("Timetable_Report.csv");
            java.io.FileWriter fw = new java.io.FileWriter(file);
            fw.write("AUTOMATED TIMETABLE GENERATION SYSTEM — WEEKLY REPORT\n");
            fw.write("Generated: " + new java.util.Date() + "\n\n");

            for (models.Classroom room : rooms) {
                fw.write("==========================================================================================\n");
                fw.write("CLASSROOM: " + room.getRoomNumber() +
                    " | CAPACITY: " + room.getCapacity() +
                    " | TYPE: " + room.getType() + "\n");
                fw.write("==========================================================================================\n");
                fw.write("Time Slot,Monday,Tuesday,Wednesday,Thursday,Friday,Saturday\n");

                Map<String, Map<String, TimetableEntry>> roomGrid = new HashMap<>();
                for (TimetableEntry e : allEntries)
                    if (e.getClassroomName().equals(room.getRoomNumber()))
                        roomGrid.computeIfAbsent(e.getTimeRange(), k -> new HashMap<>()).put(e.getDay(), e);

                Set<String> timeRanges = new TreeSet<>();
                for (models.TimeSlot s : allSlots) timeRanges.add(s.getStartTime() + " - " + s.getEndTime());

                for (String time : timeRanges) {
                    fw.write("\"" + time + "\"");
                    Map<String, TimetableEntry> dayMap = roomGrid.getOrDefault(time, new HashMap<>());
                    for (String day : DAYS) {
                        TimetableEntry e = dayMap.get(day);
                        String content = (e != null)
                            ? e.getSubjectName() + " (" + e.getTeacherName() + ")"
                            : "---";
                        fw.write(",\"" + content + "\"");
                    }
                    fw.write("\n");
                }
                fw.write("\n\n");
            }
            fw.close();
            JOptionPane.showMessageDialog(this,
                "Report saved to:\n" + file.getAbsolutePath(),
                "Export Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Export error: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
