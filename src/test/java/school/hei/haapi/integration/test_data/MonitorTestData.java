package school.hei.haapi.integration.test_data;

import static java.util.UUID.randomUUID;
import static school.hei.haapi.model.User.Role.MONITOR;

import java.time.Instant;
import school.hei.haapi.model.User;

public class MonitorTestData {
  public static User monitorOfAxel() {
    return User.builder()
        .id(randomUUID().toString())
        .firstName("Axel")
        .lastName("Monitor")
        .email("monitor.axel@mail.hei.school")
        .entranceDatetime(Instant.parse("2025-07-22T10:15:30Z"))
        .phone("+07123456789")
        .role(MONITOR)
        .build();
  }

  public static User monitorOfTolojanahary() {
    return User.builder()
        .id(randomUUID().toString())
        .firstName("Tolojanahary")
        .lastName("Monitor")
        .email("monitor.tolojanahary@mail.hei.school")
        .entranceDatetime(Instant.parse("2025-07-22T10:15:30Z"))
        .phone("+07123456789")
        .role(MONITOR)
        .build();
  }

  public void addStudentMonitor(User monitor, User student) {
    student.getMonitors().add(monitor);
  }
}
