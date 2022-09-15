package school.hei.haapi.model;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.TypeDef;
import school.hei.haapi.repository.types.PostgresEnumType;


import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "\"course\"")
@TypeDef(name = "pgsql_enum", typeClass = PostgresEnumType.class)
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

  @NotNull(message = "Ref is not null")
  private String ref;

  @NotBlank(message = "Name is mandatory")
  private String name;

  @Min(value = 1)
  private Integer credits;

  @Min(value = 1)
  private Integer totalHours;


  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}

