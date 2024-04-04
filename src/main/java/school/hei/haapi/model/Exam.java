package school.hei.haapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import static jakarta.persistence.GenerationType.IDENTITY;

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
  // todo: to review all class
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  private Integer coefficient;
  private String title;

  @ManyToOne
  @JoinColumn(name = "awarded_course_id")
  @ToString.Exclude
  private AwardedCourse awardedCourse;

  @OneToMany(mappedBy = "exam")
  @ToString.Exclude
  private List<Grade> grades;

  @Column(name = "examination_date", nullable = false)
  private Instant examinationDate;
}
