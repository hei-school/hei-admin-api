package school.hei.haapi.model;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "\"awarded_course\"")
@Getter
@Setter
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class AwardedCourse implements Serializable {
  // todo: to review all class
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "teacher_id")
  @JsonIgnoreProperties("awardedCourses")
  @ToString.Exclude
  private User mainTeacher;

  @ManyToOne
  @JoinColumn(name = "course_id")
  @ToString.Exclude
  private Course course;

  @ManyToOne
  @JoinColumn(name = "group_id")
  @ToString.Exclude
  private Group group;

  @OneToMany(mappedBy = "awardedCourse", fetch = LAZY)
  @ToString.Exclude
  private List<Exam> exams;

  @CreationTimestamp
  @Getter(AccessLevel.NONE)
  private Instant creationDatetime;
}
