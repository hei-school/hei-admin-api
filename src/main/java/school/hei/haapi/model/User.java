package school.hei.haapi.model;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import school.hei.haapi.repository.types.PostgresEnumType;

import static javax.persistence.GenerationType.IDENTITY;

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
  private String lastName;
  private String email;
  private String ref;

  @Type(type = "pgsql_enum")
  @Enumerated(EnumType.STRING)
  private Status status;

  private String phone;
  private LocalDate birthDate;
  private Instant entranceDatetime;

  @Type(type = "pgsql_enum")
  @Enumerated(EnumType.STRING)
  private Sex sex;

  private String address;

  @Column(name = "\"role\"")
  @Type(type = "pgsql_enum")
  @Enumerated(EnumType.STRING)
  private Role role;

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
    M, F
  }

  public enum Status {
    ENABLED, DISABLED
  }

  public enum Role {
    STUDENT, TEACHER, MANAGER
  }
}
