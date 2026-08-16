package school.hei.haapi.integration.testData;

import static java.time.Instant.now;
import static java.util.UUID.randomUUID;
import static school.hei.haapi.model.RetakeExamStatus.REGISTERED;

import java.time.Instant;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.RetakeExam;
import school.hei.haapi.model.RetakeExamSession;
import school.hei.haapi.model.RetakeExamStatus;
import school.hei.haapi.model.User;

public class RetakeExamTestData {
  public static RetakeExam createRetakeExam(
      User student, Course course, RetakeExamSession session) {
    return createRetakeExam(student, course, session, REGISTERED, now());
  }

  public static RetakeExam createRetakeExam(
      User student,
      Course course,
      RetakeExamSession session,
      RetakeExamStatus status,
      Instant registrationDate) {
    return RetakeExam.builder()
        .id(randomUUID().toString())
        .student(student)
        .course(course)
        .session(session)
        .status(status)
        .registrationDate(registrationDate)
        .build();
  }
}
