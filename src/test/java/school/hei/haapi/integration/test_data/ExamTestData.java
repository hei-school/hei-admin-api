package school.hei.haapi.integration.test_data;

import static java.util.UUID.randomUUID;

import java.time.Instant;
import java.util.ArrayList;
import school.hei.haapi.model.CourseAssignment;
import school.hei.haapi.model.Exam;

public class ExamTestData {
  public static Exam createExam(Instant examinationDate, CourseAssignment courseAssignment) {
    return Exam.builder()
        .id(randomUUID().toString())
        .title("Exam title")
        .grades(new ArrayList<>())
        .examinationDate(examinationDate)
        .courseAssignment(courseAssignment)
        .coefficientNumerator(2)
        .coefficientDenominator(1)
        .build();
  }
}
