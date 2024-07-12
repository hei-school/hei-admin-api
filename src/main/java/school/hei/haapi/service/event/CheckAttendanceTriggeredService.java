package school.hei.haapi.service.event;

import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.event.model.CheckAttendanceTriggered;
import school.hei.haapi.service.scheduler.AttendanceScheduler;

@Service
@AllArgsConstructor
public class CheckAttendanceTriggeredService implements Consumer<CheckAttendanceTriggered> {
  private final AttendanceScheduler attendanceScheduler;

  @Override
  public void accept(CheckAttendanceTriggered checkAttendanceTriggered) {
    attendanceScheduler.checkAttendancesEachDays();
  }
}
