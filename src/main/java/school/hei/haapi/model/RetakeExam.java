package school.hei.haapi.model;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "\"retake_exam\"")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class RetakeExam implements Serializable {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  @ManyToOne
  @JoinColumn(name = "retake_exam_session_id", nullable = false)
  private RetakeExamSession retakeExamSession;

  @ManyToOne
  @JoinColumn(name = "course_id", nullable = false)
  private Course course;

  @ManyToOne
  @JoinColumn(name = "student_id", nullable = false)
  private User student;
}
