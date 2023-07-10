package school.hei.haapi.model;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

@Entity
@Table(name = "\"exam\"")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Exam {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private String id;
  private int coefficient;
  private String title;
  @ManyToOne
  @JoinColumn(name = "course_id")
  private Course course;

  @Column(name = "examination_date", nullable = false)
  private Instant examinationDate;

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    Exam exam = (Exam) o;
    return id != null && Objects.equals(id, exam.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}

