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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "\"grade_change_history\"")
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Getter
public class GradeChangeHistory implements Serializable {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private String id;

  @ManyToOne
  @JoinColumn(name = "grade_id", updatable = false)
  private Grade grade;

  private Double score;

  @EqualsAndHashCode.Exclude @CreationTimestamp private Instant changeInstant;

  private String comment;

  public GradeChangeHistory(Grade grade, Double score, String comment) {
    this.grade = grade;
    this.score = score;
    this.comment = comment;
  }
}
