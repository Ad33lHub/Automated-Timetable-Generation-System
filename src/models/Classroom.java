package models;

public class Classroom {
    private int id;
    private String roomNumber;
    private int capacity;
    private String type; // Lecture Hall, Laboratory, Seminar Room

    public Classroom() {}

    public Classroom(int id, String roomNumber, int capacity, String type) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.capacity = capacity;
        this.type = type;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    @Override
    public String toString() { return roomNumber + " (" + type + ")"; }
}
