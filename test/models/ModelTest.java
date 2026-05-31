package models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Domain Models Unit Tests")
public class ModelTest {

    @Test
    @DisplayName("Verify Teacher Model - Getters, Setters, and Constructor")
    public void testTeacherModel() {
        Teacher teacher = new Teacher(12, "Dr. John Smith", "Software Architecture", "Tuesday, Thursday");

        assertEquals(12, teacher.getId());
        assertEquals("Dr. John Smith", teacher.getName());
        assertEquals("Software Architecture", teacher.getSpecialization());
        assertEquals("Tuesday, Thursday", teacher.getAvailability());

        // Test Setters
        teacher.setId(45);
        teacher.setName("Dr. Sarah Jane");
        teacher.setSpecialization("Database Systems");
        teacher.setAvailability("Monday, Friday");

        assertEquals(45, teacher.getId());
        assertEquals("Dr. Sarah Jane", teacher.getName());
        assertEquals("Database Systems", teacher.getSpecialization());
        assertEquals("Monday, Friday", teacher.getAvailability());
        
        // Test toString()
        assertEquals("Dr. Sarah Jane", teacher.toString(), "toString() should return the name");
    }

    @Test
    @DisplayName("Verify Classroom Model - Getters, Setters, and Constructor")
    public void testClassroomModel() {
        Classroom classroom = new Classroom(5, "Lab 3", 40, "Laboratory");

        assertEquals(5, classroom.getId());
        assertEquals("Lab 3", classroom.getRoomNumber());
        assertEquals(40, classroom.getCapacity());
        assertEquals("Laboratory", classroom.getType());

        // Test Setters
        classroom.setId(9);
        classroom.setRoomNumber("Room 401");
        classroom.setCapacity(60);
        classroom.setType("Lecture Hall");

        assertEquals(9, classroom.getId());
        assertEquals("Room 401", classroom.getRoomNumber());
        assertEquals(60, classroom.getCapacity());
        assertEquals("Lecture Hall", classroom.getType());

        // Test toString()
        assertEquals("Room 401 (Lecture Hall)", classroom.toString(), "toString() should match formatted room specification");
    }

    @Test
    @DisplayName("Verify TimeSlot Model - Getters, Setters, and Constructor")
    public void testTimeSlotModel() {
        TimeSlot slot = new TimeSlot(1, "Monday", "08:30", "10:00");

        assertEquals(1, slot.getId());
        assertEquals("Monday", slot.getDay());
        assertEquals("08:30", slot.getStartTime());
        assertEquals("10:00", slot.getEndTime());

        // Test Setters
        slot.setId(22);
        slot.setDay("Wednesday");
        slot.setStartTime("14:00");
        slot.setEndTime("15:30");

        assertEquals(22, slot.getId());
        assertEquals("Wednesday", slot.getDay());
        assertEquals("14:00", slot.getStartTime());
        assertEquals("15:30", slot.getEndTime());

        // Test toString()
        assertEquals("Wednesday 14:00 - 15:30", slot.toString(), "toString() should match formatted time range");
    }
}
