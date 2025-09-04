package school.hei.haapi.model;

import static java.util.Comparator.comparing;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.math.Fraction;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.CreationTimestamp;
import school.hei.haapi.model.exception.ExamsCoefficientSumZero;

@Entity
@Table(name = "\"grade\"")
@Getter
@Setter
@ToString
@Builder(toBuilder = true)
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

  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  @Column(updatable = false)
  private Double score;

  @Column(updatable = false)
  @EqualsAndHashCode.Exclude
  @CreationTimestamp
  private Instant creationDatetime;

  @OneToMany(mappedBy = "grade")
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @Cascade(CascadeType.ALL)
  private final List<GradeChangeHistory> gradeChangeHistories = new ArrayList<>();

  public Grade(Exam exam, User student, double score) {
    this.score = score;
    this.student = student;
    this.exam = exam;
  }

  public void setScore(Double score, String comment) {
    this.gradeChangeHistories.add(new GradeChangeHistory(this, score, comment));
  }

  public Double getScore() {
    return getLastChange().map(GradeChangeHistory::getScore).orElse(getInitialScore());
  }

  private Optional<GradeChangeHistory> getLastChange() {
    return gradeChangeHistories.stream().max(comparing(GradeChangeHistory::getChangeInstant));
  }

  public Instant getUpdateDatetime() {
    return getLastChange().map(GradeChangeHistory::getChangeInstant).orElse(creationDatetime);
  }

  public Double getInitialScore() {
    return score;
  }

  public static Optional<Fraction> weightedAverageOfGrades(List<Grade> grades) {
    var sumCoefficients =
        grades.stream()
            .map(grade -> grade.getExam().getCoefficientFraction())
            .reduce(Fraction::add);

    if (sumCoefficients.isEmpty() || sumCoefficients.get().compareTo(Fraction.ZERO) == 0)
      throw new ExamsCoefficientSumZero();

    var weightedSum =
        grades.stream()
            .map(
                grade -> {
                  var coefficientFrac = grade.getExam().getCoefficientFraction();
                  return coefficientFrac.multiplyBy(Fraction.getFraction(grade.getScore()));
                })
            .reduce(Fraction::add);

    return weightedSum.map(fraction -> fraction.divideBy(sumCoefficients.get()));
  }
}
