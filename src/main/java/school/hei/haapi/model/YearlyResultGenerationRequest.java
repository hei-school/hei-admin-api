package school.hei.haapi.model;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static org.hibernate.type.SqlTypes.NAMED_ENUM;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import school.hei.haapi.endpoint.rest.model.YearlyResultGenerationStatus;

@Entity
@Table(name = "yearly_result_generation_request")
@Builder
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@ToString
public class YearlyResultGenerationRequest {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  @Enumerated(STRING)
  @JdbcTypeCode(NAMED_ENUM)
  private YearlyResultGenerationStatus status;

  private Instant datetime;

  private String fileName;

  @ManyToOne private FileInfo fileInfo;
}
