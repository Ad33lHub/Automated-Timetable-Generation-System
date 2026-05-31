package models;

public class TimetableEntry {
    private int id;
    private String day;
    private int timeSlotId;
    private int subjectId;
    private int teacherId;
    private int classroomId;
    
    // Display fields
    private String timeRange;
    private String subjectName;
    private String teacherName;
    private String classroomName;
    private String sectionName;

    public TimetableEntry() {}

    public TimetableEntry(int id, String day, int timeSlotId, int subjectId, int teacherId, int classroomId) {
        this.id = id;
        this.day = day;
        this.timeSlotId = timeSlotId;
        this.subjectId = subjectId;
        this.teacherId = teacherId;
        this.classroomId = classroomId;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getDay() { return day; }
    public void setDay(String day) { this.day = day; }
    public int getTimeSlotId() { return timeSlotId; }
    public void setTimeSlotId(int timeSlotId) { this.timeSlotId = timeSlotId; }
    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }
    public int getTeacherId() { return teacherId; }
    public void setTeacherId(int teacherId) { this.teacherId = teacherId; }
    public int getClassroomId() { return classroomId; }
    public void setClassroomId(int classroomId) { this.classroomId = classroomId; }

    public String getTimeRange() { return timeRange; }
    public void setTimeRange(String timeRange) { this.timeRange = timeRange; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    public String getClassroomName() { return classroomName; }
    public void setClassroomName(String classroomName) { this.classroomName = classroomName; }
    public String getSectionName() { return sectionName; }
    public void setSectionName(String sectionName) { this.sectionName = sectionName; }
}
