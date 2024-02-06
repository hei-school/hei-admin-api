package school.hei.haapi.model;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.IDENTITY;

import java.time.Instant;
import javax.persistence.Entity;
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
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import school.hei.haapi.endpoint.rest.model.FeeTypeEnum;
import school.hei.haapi.repository.types.PostgresEnumType;

@Entity
@Table(name = "\"fee_template\"")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TypeDef(name = "pgsql_enum", typeClass = PostgresEnumType.class)
public class FeeTemplate {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  private String name;

  @CreationTimestamp private Instant creationDatetime;

  private Integer amount;

  private Integer numberOfPayments;

  @Type(type = "pgsql_enum")
  @Enumerated(STRING)
  private FeeTypeEnum type;
}
