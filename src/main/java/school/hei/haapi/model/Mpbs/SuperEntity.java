package school.hei.haapi.model.Mpbs;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static org.hibernate.type.SqlTypes.NAMED_ENUM;

import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import school.hei.haapi.endpoint.rest.model.MobileMoneyType;

@MappedSuperclass
@Getter
@Setter
public class SuperEntity {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  @JoinColumn(name = "psp_id")
  private String pspId;

  @JdbcTypeCode(NAMED_ENUM)
  @Enumerated(STRING)
  @JoinColumn(name = "mobile_money_type")
  private MobileMoneyType mobileMoneyType;

  @JoinColumn(name = "creation_datetime")
  @CreationTimestamp
  private Instant creationDatetime;
}
