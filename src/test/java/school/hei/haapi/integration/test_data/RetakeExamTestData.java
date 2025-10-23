package school.hei.haapi.integration.test_data;

import static school.hei.haapi.model.RetakeExamStatus.REGISTERED;

import school.hei.haapi.model.Course;
import school.hei.haapi.model.RetakeExam;
import school.hei.haapi.model.RetakeExamSession;
import school.hei.haapi.model.User;

public class RetakeExamTestData {
  public static RetakeExam createRetakeExam(
      User student, Course course, RetakeExamSession session) {
    return RetakeExam.builder()
        .id("retakeExam%s".formatted(course.getId()))
        .student(student)
        .course(course)
        .session(session)
        .status(REGISTERED)
        .build();
  }
}
