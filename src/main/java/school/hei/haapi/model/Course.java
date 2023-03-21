package school.hei.haapi.model;

import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import school.hei.haapi.repository.types.PostgresEnumType;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import static javax.persistence.GenerationType.IDENTITY;
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
import org.hibernate.annotations.TypeDef;
import school.hei.haapi.repository.types.PostgresEnumType;

@Entity
@Table(name = "\"course\"")
@Getter
@Setter
@TypeDef(name = "pgsql_enum", typeClass = PostgresEnumType.class)
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Course implements Serializable {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;
  private String code;
  private String name;
  private int credits;
  private int totals_hours;

  @ManyToOne
  @JoinColumn(name = "main_teacher_id", nullable = false)
  private User main_teacher;

  @ManyToMany
  @JoinTable(name="course_student")
  private List<User> students;

  @Type(type = "pgsql_enum")
  @Enumerated(EnumType.STRING)
  private school.hei.haapi.endpoint.rest.model.CourseStatus status;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Course course = (Course) o;
    return credits == course.credits && totals_hours == course.totals_hours && id.equals(course.id) &&
            code.equals(course.code) && name.equals(course.name) && main_teacher.equals(course.main_teacher)
            && status == course.status;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, code, name, credits, totals_hours, main_teacher, status);
  }
}