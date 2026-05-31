package models;

public class Subject {
    private int id;
    private String name;
    private String code;
    private int creditHours;
    private String department;
    private String section;

    public Subject() {}

    public Subject(int id, String name, String code, int creditHours, String department, String section) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.creditHours = creditHours;
        this.department = department;
        this.section = section;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public int getCreditHours() { return creditHours; }
    public void setCreditHours(int creditHours) { this.creditHours = creditHours; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    @Override
    public String toString() { return code + " - " + name; }
}
