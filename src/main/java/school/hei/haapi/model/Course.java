package school.hei.haapi.model;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "\"course\"")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Course implements Serializable {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  private String code;

  private String name;

  private Integer credits;

  private Integer totalHours;

  @OneToMany(mappedBy = "course", fetch = LAZY)
  private List<AwardedCourse> awardedCourse;


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    Course course = (Course) o;
    return id != null && Objects.equals(id, course.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

}
