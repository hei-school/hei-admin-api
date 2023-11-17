package school.hei.haapi.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "\"exam\"")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Exam implements Serializable {
  //todo: to review all class
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;
  private Integer coefficient;
  private String title;
  @ManyToOne
  @JoinColumn(name = "awarded_course_id")
  private AwardedCourse awardedCourse;

  @OneToMany(mappedBy = "exam")
  private List<Grade> grades;

  @Column(name = "examination_date", nullable = false)
  private Instant examinationDate;
}
