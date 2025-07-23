package school.hei.haapi.integration.test_data;

import static java.util.UUID.randomUUID;

import java.time.Instant;
import java.util.List;
import school.hei.haapi.model.CourseAssignment;
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.Grade;

public class ExamTestData {
  public static Exam createExam(
      Instant examinationDate, CourseAssignment courseAssignment, List<Grade> grades) {
    return Exam.builder()
        .id(randomUUID().toString())
        .title("Exam title")
        .grades(grades)
        .examinationDate(examinationDate)
        .courseAssignment(courseAssignment)
        .coefficient(2)
        .build();
  }
}
