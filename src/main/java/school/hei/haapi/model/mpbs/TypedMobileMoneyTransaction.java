package school.hei.haapi.model.mpbs;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static org.hibernate.type.SqlTypes.NAMED_ENUM;

import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import school.hei.haapi.endpoint.rest.model.MobileMoneyType;

@MappedSuperclass
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TypedMobileMoneyTransaction {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  private String pspId;

  @JdbcTypeCode(NAMED_ENUM)
  @Enumerated(STRING)
  private MobileMoneyType mobileMoneyType;

  @CreationTimestamp private Instant creationDatetime;
}
