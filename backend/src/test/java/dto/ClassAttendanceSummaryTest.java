package dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClassAttendanceSummaryTest {

    // helper constructor-style setup via setters (if needed later)
    private ClassAttendanceSummary create(int present, int absent, int excused) {
        ClassAttendanceSummary s = new ClassAttendanceSummary();

        setField(s, "present", present);
        setField(s, "absent", absent);
        setField(s, "excused", excused);

        return s;
    }

    // still using reflection BUT only in ONE place (minimized damage)
    private void setField(ClassAttendanceSummary s, String name, int value) {
        try {
            var f = ClassAttendanceSummary.class.getDeclaredField(name);
            f.setAccessible(true);
            f.setInt(s, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void getRate_zeroTotal_returnsZero() {
        ClassAttendanceSummary s = create(0, 0, 0);
        assertEquals(0.0, s.getRate(), 1e-9);
    }

    @Test
    void getRate_allPresent_returns100() {
        ClassAttendanceSummary s = create(7, 0, 0);
        assertEquals(100.0, s.getRate(), 1e-9);
    }

    @Test
    void getRate_mixedValues() {
        ClassAttendanceSummary s = create(3, 2, 1);

        double expected = (3 * 100.0) / 6;
        assertEquals(expected, s.getRate(), 1e-9);
    }

    @Test
    void getRate_largeNumbers_noOverflow() {
        int present = Integer.MAX_VALUE / 2;
        int absent = Integer.MAX_VALUE / 4;
        int excused = Integer.MAX_VALUE / 4;

        ClassAttendanceSummary s = create(present, absent, excused);

        double total = (double) present + absent + excused;
        double expected = (present * 100.0) / total;

        assertEquals(expected, s.getRate(), 1e-9);
    }

    @Test
    void getRate_allAbsent() {
        ClassAttendanceSummary s = create(0, 10, 0);

        assertEquals(0.0, s.getRate(), 1e-9);
    }

    @Test
    void getters_shouldReturnValues() {
        ClassAttendanceSummary s = new ClassAttendanceSummary();

        setObjectField(s, "classId", 10L);
        setObjectField(s, "className", "Math");

        setField(s, "present", 5);
        setField(s, "absent", 3);
        setField(s, "excused", 2);

        assertEquals(10L, s.getClassId());
        assertEquals("Math", s.getClassName());
        assertEquals(5, s.getPresent());
        assertEquals(3, s.getAbsent());
        assertEquals(2, s.getExcused());
    }

    private void setObjectField(ClassAttendanceSummary s, String name, Object value) {
        try {
            var f = ClassAttendanceSummary.class.getDeclaredField(name);
            f.setAccessible(true);
            f.set(s, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}