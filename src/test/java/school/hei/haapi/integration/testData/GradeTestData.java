package school.hei.haapi.integration.testData;

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

  /**
   * Scores are whole numbers on purpose: the weighted average goes through {@link
   * org.apache.commons.lang3.math.Fraction}, and a score with a long decimal expansion turns into a
   * fraction whose denominator overflows an int as soon as a few of them are summed.
   */
  public static List<Grade> createRandomGrades(List<User> students, Exam exam) {
    var random = new Random();
    return students.stream()
        .map(
            student ->
                Grade.builder()
                    .id(randomUUID().toString())
                    .exam(exam)
                    .student(student)
                    .score((double) random.nextInt(5, 20))
                    .build())
        .toList();
  }
}
