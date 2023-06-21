package school.hei.haapi.model;

import java.time.LocalDateTime;
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
  private Course courseId;

  @Column(name = "examination_date", nullable = false)
  private LocalDateTime examinationDate;
}

