package school.hei.haapi.model;

import static jakarta.persistence.EnumType.STRING;
import static org.hibernate.type.SqlTypes.NAMED_ENUM;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import school.hei.haapi.endpoint.rest.model.FeeStatusEnum;

@Entity
@Table(name = "\"fee_status_history\"")
@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class FeeStatusHistory {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private String id;

  @CreationTimestamp private Instant datetime;

  @JdbcTypeCode(NAMED_ENUM)
  @Enumerated(STRING)
  private FeeStatusEnum status;

  @ManyToOne
  @JoinColumn(name = "fee_id")
  @ToString.Exclude
  @JsonIgnore
  private Fee fee;
}
