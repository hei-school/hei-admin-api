package school.hei.haapi.model;

import static java.math.BigDecimal.ZERO;
import static java.math.MathContext.*;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import school.hei.haapi.model.exception.CourseCoefficientsSumZero;

@Entity
@Table(name = "\"grade\"")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Grade implements Serializable {
  // todo: to review all class
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private String id;

  @ManyToOne
  @JoinColumn(name = "student_id", updatable = false)
  private User student;

  @ManyToOne
  @JoinColumn(name = "exam_id", updatable = false)
  private Exam exam;

  private Double score;
  @CreationTimestamp private Instant creationDatetime;

  public Grade(Exam exam, User student) {
    this.score = 0.0;
    this.student = student;
    this.exam = exam;
  }

  public static BigDecimal weightedAverageOfGrades(List<Grade> grades) {
    var sumCoefficients =
        BigDecimal.valueOf(
            grades.stream().map(Grade::getExam).mapToInt(Exam::getCoefficient).sum());
    var weightedSum =
        grades.stream()
            .map(
                grade ->
                    BigDecimal.valueOf(grade.getExam().getCoefficient())
                        .multiply(BigDecimal.valueOf(grade.getScore())))
            .reduce(BigDecimal::add)
            .orElse(ZERO);

    if (ZERO.equals(weightedSum)) throw new CourseCoefficientsSumZero();

    return weightedSum.divide(sumCoefficients, UNLIMITED);
  }
}
