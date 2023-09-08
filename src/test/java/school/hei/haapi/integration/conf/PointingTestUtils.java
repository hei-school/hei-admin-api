package school.hei.haapi.integration.conf;

import static school.hei.haapi.endpoint.rest.model.EnableStatus.ENABLED;
import static school.hei.haapi.endpoint.rest.model.Student.SexEnum.F;
import static school.hei.haapi.integration.conf.TestUtils.course1;
import static school.hei.haapi.integration.conf.TestUtils.course2;
import java.time.Instant;
import java.time.LocalDate;
import school.hei.haapi.endpoint.rest.model.Pointing;
import school.hei.haapi.endpoint.rest.model.Student;
import school.hei.haapi.endpoint.rest.model.StudentMissing;

public class PointingTestUtils {
  public static Student student1() {
    return new Student()
        .id("student1_id")
        .address("Adr 1")
        .birthDate(LocalDate.parse("2000-01-01"))
        .entranceDatetime(Instant.parse("2021-11-08T08:25:24.00Z"))
        .sex(Student.SexEnum.M)
        .email("test+ryan@hei.school")
        .firstName("Ryan")
        .lastName("Andria")
        .ref("STD21001")
        .phone("0322411123")
        .status(ENABLED);
  }

  public static Student student2() {
    return new Student()
        .id("student2_id")
        .address("Adr 2")
        .birthDate(LocalDate.parse("2000-01-02"))
        .entranceDatetime(Instant.parse("2021-11-09T08:26:24.00Z"))
        .sex(F)
        .email("test+student2@hei.school")
        .firstName("Two")
        .lastName("Student")
        .ref("STD21002")
        .phone("0322411124")
        .status(ENABLED);
  }

  public static Student student3() {
    return new Student()
        .id("student3_id")
        .address("Adr 2")
        .birthDate(LocalDate.parse("2000-01-02"))
        .entranceDatetime(Instant.parse("2021-11-09T08:26:24.00Z"))
        .sex(F)
        .email("test+student3@hei.school")
        .firstName("Three")
        .lastName("Student")
        .ref("STD21003")
        .phone("0322411124")
        .status(ENABLED);
  }

  public static StudentMissing pointing1Ok() {
    return new StudentMissing()
        .id("pointing1_id")
        .student(student1())
        .place("Andraharo")
        .isMissing(false)
        .isLate(false)
        .course(course1())
        .pointingDatetime(Instant.parse("2021-11-08T08:25:24.00Z"))
        .datetimeCourseEnter(Instant.parse("2021-11-08T09:00:24.00Z"))
        .datetimeCourseExit(Instant.parse("2021-11-08T12:00:00.00Z"));
  }

  public static StudentMissing pointing2Ok() {
    return new StudentMissing()
        .id("pointing2_id")
        .student(student1())
        .place("Andraharo")
        .isMissing(false)
        .isLate(false)
        .course(course1())
        .pointingDatetime(Instant.parse("2021-08-08T08:25:24.00Z"))
        .datetimeCourseEnter(Instant.parse("2021-08-08T09:00:24.00Z"))
        .datetimeCourseExit(Instant.parse("2021-08-08T12:00:00.00Z"));
  }

  public static StudentMissing pointing3Late() {
    return new StudentMissing()
        .id("pointing3_id")
        .place("Ivandry")
        .isLate(true)
        .isMissing(false)
        .course(course1())
        .student(student2())
        .pointingDatetime(Instant.parse("2021-11-08T09:25:24.00Z"))
        .datetimeCourseEnter(Instant.parse("2021-11-08T09:00:24.00Z"))
        .datetimeCourseExit(Instant.parse("2021-11-08T12:00:00.00Z"));
  }

  public static StudentMissing pointing4Late() {
    return new StudentMissing()
        .id("pointing4_id")
        .isLate(true)
        .place("Andraharo")
        .isMissing(false)
        .course(course2())
        .student(student2())
        .pointingDatetime(Instant.parse("2021-11-08T10:30:24.00Z"))
        .datetimeCourseEnter(Instant.parse("2021-11-08T010:00:24.00Z"))
        .datetimeCourseExit(Instant.parse("2021-11-08T12:00:00.00Z"));
  }

  public static StudentMissing pointing5Late() {
    return new StudentMissing()
        .id("pointing5_id")
        .isLate(true)
        .place("Ivandry")
        .isMissing(false)
        .student(student2())
        .course(course1())
        .pointingDatetime(Instant.parse("2022-01-08T08:15:00.00Z"))
        .datetimeCourseEnter(Instant.parse("2022-01-08T08:00:00.00Z"))
        .datetimeCourseExit(Instant.parse("2022-01-08T10:00:00.00Z"));
  }

  public static StudentMissing pointing6Missing() {
    return new StudentMissing()
        .id("pointing6_id")
        .isMissing(true)
        .isLate(false)
        .student(student3())
        .course(course1())
        .pointingDatetime(null)
        .datetimeCourseEnter(Instant.parse("2021-11-08T09:00:24.00Z"))
        .datetimeCourseExit(Instant.parse("2021-11-08T12:00:00.00Z"));
  }

  public static StudentMissing pointing7Missing() {
    return new StudentMissing()
        .id("pointing7_id")
        .isMissing(true)
        .isLate(false)
        .student(student3())
        .course(course1())
        .pointingDatetime(null)
        .datetimeCourseEnter(Instant.parse("2021-11-09T09:00:24.00Z"))
        .datetimeCourseExit(Instant.parse("2021-11-09T12:00:00.00Z"));
  }

  public static Pointing pointingCreated() {
    return new Pointing()
        .id();
  }
}
