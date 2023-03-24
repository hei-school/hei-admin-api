package school.hei.haapi.model;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

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

  @NotBlank(message = "code is mandatory")
  @Column(unique = true)
  private String code;

  @NotBlank(message = "name is mandatory")
  @Column(unique = true)
  private String name;

  @NotBlank(message = "credits is mandatory")
  private Integer credits;

  @NotBlank(message = "total hours is mandatory")
  private Integer totalHours;

  @ManyToOne
  @JoinColumn(name = "main_teacher_id", nullable = false)
  private User teacher;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    Course user = (Course) o;
    return id != null && Objects.equals(id, user.id) ||
        code != null && Objects.equals(code, user.code);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}