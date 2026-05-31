package models;

public class Student {
    private int id;
    private String studentId;
    private String name;
    private String password;
    private String department;
    private String section;

    public Student() {}

    public Student(int id, String studentId, String name, String password, String department, String section) {
        this.id = id;
        this.studentId = studentId;
        this.name = name;
        this.password = password;
        this.department = department;
        this.section = section;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }
}
