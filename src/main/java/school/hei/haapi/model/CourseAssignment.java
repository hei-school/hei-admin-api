package school.hei.haapi.model;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
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
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "\"course_assignment\"")
@Getter
@Setter
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@SQLDelete(sql = "update \"course_assignment\" set is_deleted = true where id = ?")
@SQLRestriction("is_deleted = false")
public class CourseAssignment implements Serializable {
  // todo: to review all class
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "teacher_id")
  @JsonIgnoreProperties("courseAssignments")
  @ToString.Exclude
  private User mainTeacher;

  @ManyToOne
  @JoinColumn(name = "course_id")
  @ToString.Exclude
  private Course course;

  @ManyToMany
  @JoinTable(
      name = "course_assignment_group",
      joinColumns = @JoinColumn(name = "course_assignment_id"),
      inverseJoinColumns = @JoinColumn(name = "group_id"))
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private List<Group> groups;

  @OneToMany(mappedBy = "courseAssignment", fetch = LAZY)
  @ToString.Exclude
  private List<Exam> exams;

  @EqualsAndHashCode.Exclude @CreationTimestamp private Instant creationDatetime;

  @EqualsAndHashCode.Exclude @Builder.Default private boolean isDeleted = false;
}
