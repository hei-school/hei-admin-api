package school.hei.haapi.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

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

  public static double scoreSumWithCoefficient(List<Grade> grades) {
    return grades.stream()
        .mapToDouble(
            grade ->
                Optional.ofNullable(grade.getScore()).orElse(0.)
                    * Optional.ofNullable(grade.getExam().getCoefficient()).orElse(0))
        .sum();
  }
}
