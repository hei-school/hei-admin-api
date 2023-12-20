package school.hei.haapi.model;

import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import school.hei.haapi.endpoint.rest.model.AcademicSector;
import school.hei.haapi.endpoint.rest.security.model.Role;
import school.hei.haapi.repository.types.PostgresEnumType;
import school.hei.haapi.service.utils.DataFormatterUtils;

@Entity
@Table(name = "\"user\"")
@TypeDef(name = "pgsql_enum", typeClass = PostgresEnumType.class)
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User implements Serializable {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  private String firstName;

  @NotBlank(message = "Last name is mandatory")
  private String lastName;

  @NotBlank(message = "Email is mandatory")
  @Email(message = "Email must be valid")
  private String email;

  @NotBlank(message = "Reference is mandatory")
  private String ref;

  private String nic;

  @Type(type = "pgsql_enum")
  @Enumerated(EnumType.STRING)
  private Status status;

  private String phone;

  private LocalDate birthDate;

  private String birthPlace;

  private Instant entranceDatetime;

  @Type(type = "pgsql_enum")
  @Enumerated(EnumType.STRING)
  private AcademicSector academicSector;

  @Type(type = "pgsql_enum")
  @Enumerated(EnumType.STRING)
  private Sex sex;

  private String address;

  @Column(name = "\"role\"")
  @Type(type = "pgsql_enum")
  @Enumerated(EnumType.STRING)
  private Role role;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "mainTeacher")
  private List<AwardedCourse> awardedCourses;

  @OneToMany(mappedBy = "student", fetch = LAZY)
  private List<GroupFlow> groupFlows;

  @OneToMany(mappedBy = "student", fetch = LAZY)
  private List<Grade> grades;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    User user = (User) o;
    return id != null && Objects.equals(id, user.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  public enum Sex {
    M,
    F;

    public static Sex fromValue(String value) {
      return DataFormatterUtils.fromValue(Sex.class, value);
    }
  }

  public enum Status {
    ENABLED,
    DISABLED,
    SUSPENDED;

    public static Status fromValue(String value) {
      return DataFormatterUtils.fromValue(Status.class, value);
    }
  }

  public enum Role {
    STUDENT,
    TEACHER,
    MANAGER;

    public static Role fromValue(String value) {
      return DataFormatterUtils.fromValue(Role.class, value);
    }
  }
}
