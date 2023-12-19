package school.hei.haapi.unit.utils;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import static org.junit.jupiter.api.Assertions.assertEquals;

import static school.hei.haapi.service.utils.InstantUtils.getNextDueDateTime;

public class InstantUtilsTest {

    @Test
    void jump_to_next_end_of_month_with_31_days_with_isEndOfMonth_true(){
        Instant instant = Instant.parse("2021-11-08T08:25:24.00Z");
        Instant actual = getNextDueDateTime(instant, true, 1);
        Instant expected = Instant.parse("2021-12-31T00:00:00.00Z");
        assertEquals(expected, actual);
    }

    @Test
    void get_end_of_month_with_30_days_with_isEndOfMonth_true(){
        Instant instant = Instant.parse("2021-10-08T08:25:24.00Z");
        Instant actual = getNextDueDateTime(instant, true, 1);
        Instant expected = Instant.parse("2021-11-30T00:00:00.00Z");
        assertEquals(expected, actual);
    }

    @Test
    void get_next_due_datetime_with_isEndOfMonth_false(){
        Instant instant = Instant.parse("2021-10-08T08:25:24.00Z");
        Instant actual = getNextDueDateTime(instant, false, 1);
        Instant expected = Instant.parse("2021-11-08T00:00:00.00Z");
        assertEquals(expected, actual);
    }

    @Test
    void get_to_end_of_february_month_isEndOfMonth_true(){
        Instant instant = Instant.parse("2021-01-08T08:25:24.00Z");
        Instant actual = getNextDueDateTime(instant, true, 1);
        Instant expected = Instant.parse("2021-02-28T00:00:00.00Z");
        assertEquals(expected, actual);
    }

    @Test
    void get_to_end_of_february_month_with_isEndOfMonth_false(){
        Instant instant = Instant.parse("2021-01-30T08:25:24.00Z");
        Instant actual = getNextDueDateTime(instant, false, 1);
        Instant expected = Instant.parse("2021-02-28T00:00:00.00Z");
        assertEquals(expected, actual);
    }

}
