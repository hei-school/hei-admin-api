package school.hei.haapi.integration.test_data;

import static school.hei.haapi.integration.test_data.CourseTestData.ia1;
import static school.hei.haapi.integration.test_data.CourseTestData.prog1;
import static school.hei.haapi.integration.test_data.CourseTestData.prog3;
import static school.hei.haapi.integration.test_data.RetakeExamSessionTestData.session1;
import static school.hei.haapi.integration.test_data.RetakeExamSessionTestData.session2;
import static school.hei.haapi.integration.test_data.StudentTestData.axel;
import static school.hei.haapi.model.RetakeExamStatus.REGISTERED;

import lombok.AllArgsConstructor;
import school.hei.haapi.model.RetakeExam;

@AllArgsConstructor
public class RetakeExamTestData {
  public static RetakeExam retakeExamProg1() {
    return RetakeExam.builder()
        .id("retakeExamProg1")
        .student(axel())
        .course(prog1())
        .session(session1())
        .status(REGISTERED)
        .build();
  }

  public static RetakeExam retakeExamProg3() {
    return RetakeExam.builder()
        .id("retakeExamProg3")
        .student(axel())
        .course(prog3())
        .session(session1())
        .status(REGISTERED)
        .build();
  }

  public static RetakeExam retakeExamIa1() {
    return RetakeExam.builder()
        .id("retakeExamIa1")
        .student(axel())
        .course(ia1())
        .session(session2())
        .status(REGISTERED)
        .build();
  }
}
