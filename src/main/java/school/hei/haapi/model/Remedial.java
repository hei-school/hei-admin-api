package school.hei.haapi.model;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "\"remedial\"")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Remedial implements Serializable {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  private String title;

  @ManyToOne
  @JoinColumn(name = "course_assignment_id")
  @ToString.Exclude
  private CourseAssignment courseAssignment;

  @Column(name = "remedial_date", nullable = false)
  private Instant remedialDate;

  @ManyToMany
  @JoinTable(
      name = "student_remedials",
      joinColumns = @JoinColumn(name = "remedial_id"),
      inverseJoinColumns = @JoinColumn(name = "user_id"))
  private List<User> students;
}
