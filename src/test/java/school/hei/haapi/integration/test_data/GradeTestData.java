package school.hei.haapi.integration.test_data;

import static java.util.UUID.randomUUID;

import java.time.Instant;
import java.util.List;
import java.util.Random;
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.Grade;
import school.hei.haapi.model.User;

public class GradeTestData {
  public static Grade createGrade(User student, Exam exam, double score) {
    return Grade.builder()
        .id(randomUUID().toString())
        .exam(exam)
        .student(student)
        .creationDatetime(Instant.parse("2025-07-22T10:00:00Z"))
        .score(score)
        .build();
  }

  public static List<Grade> createRandomGrades(List<User> students, Exam exam) {
    return students.stream()
        .map(
            student ->
                Grade.builder()
                    .id(randomUUID().toString())
                    .exam(exam)
                    .student(student)
                    .score(new Random().nextDouble(5, 20))
                    .build())
        .toList();
  }
}
