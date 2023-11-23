package school.hei.haapi.unit.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import school.hei.haapi.endpoint.event.gen.CheckAttendanceTriggered;
import school.hei.haapi.service.event.CheckAttendanceTriggeredService;
import school.hei.haapi.service.scheduler.AttendanceScheduler;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class CheckAttendanceTriggeredServiceTest {

  @Mock
  private AttendanceScheduler attendanceScheduler;

  private CheckAttendanceTriggeredService subject;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    subject = new CheckAttendanceTriggeredService(attendanceScheduler);
  }

  @Test
  public void checkAttendanceTriggered_accept_ok() {
    CheckAttendanceTriggered checkAttendanceTriggered = new CheckAttendanceTriggered();
    subject.accept(checkAttendanceTriggered);

    verify(attendanceScheduler, times(1)).checkAttendancesEachDays();
  }
}
