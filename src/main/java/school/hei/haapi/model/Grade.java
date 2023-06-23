package school.hei.haapi.model;

import java.time.LocalDateTime;
import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

@Entity
@Table(name = "\"grade\"")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Grade {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private String id;
  @ManyToOne
  @JoinColumn(name = "user_id")
  private StudentCourse userId;
  @OneToOne
  @JoinColumn(name = "exam_id")
  private Exam examId;
  private int score;
  private LocalDateTime creationDateTime;

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    Grade grade = (Grade) o;
    return id != null && Objects.equals(id, grade.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
