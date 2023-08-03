package school.hei.haapi.model;

import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import school.hei.haapi.repository.types.PostgresEnumType;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "\"transcript\"")
@Getter
@Setter
@ToString
@TypeDef(name = "pgsql_enum", typeClass = PostgresEnumType.class)
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Transcript implements Serializable {
  @Id
  private String id;

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "student_id")
  private User student;

  @Type(type = "pgsql_enum")
  @Enumerated(EnumType.STRING)
  private school.hei.haapi.endpoint.rest.model.Transcript.SemesterEnum semester;

  private Integer academicYear;

  private Boolean isDefinitive;

  private Instant creationDatetime;

  public static final String CREATION_DATETIME = "creationDatetime";
}
