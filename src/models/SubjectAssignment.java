package models;

public class SubjectAssignment {
    private int id;
    private int teacherId;
    private int subjectId;
    private String teacherName;
    private String subjectName;
    private String subjectSection;

    public SubjectAssignment() {}

    public SubjectAssignment(int id, int teacherId, int subjectId, String teacherName, String subjectName, String subjectSection) {
        this.id = id;
        this.teacherId = teacherId;
        this.subjectId = subjectId;
        this.teacherName = teacherName;
        this.subjectName = subjectName;
        this.subjectSection = subjectSection;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTeacherId() { return teacherId; }
    public void setTeacherId(int teacherId) { this.teacherId = teacherId; }

    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public String getSubjectSection() { return subjectSection; }
    public void setSubjectSection(String subjectSection) { this.subjectSection = subjectSection; }
}
