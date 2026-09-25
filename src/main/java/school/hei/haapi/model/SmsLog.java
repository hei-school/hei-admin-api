package school.hei.haapi.model;

import static jakarta.persistence.EnumType.STRING;
import static org.hibernate.type.SqlTypes.NAMED_ENUM;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;

@Table(name = "sms_log")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
public class SmsLog implements Serializable {
  @Id private String id;

  @ManyToOne
  @JoinColumn(name = "campaign_id")
  private SmsCampaign campaign;

  private String phoneNumber;

  @Enumerated(STRING)
  @JdbcTypeCode(NAMED_ENUM)
  private SmsMessageStatus status;

  @Enumerated(STRING)
  @JdbcTypeCode(NAMED_ENUM)
  private SmsRecipientSource recipientSource;

  @ManyToOne
  @JoinColumn(name = "contact_id")
  private SmsContact contact;

  private Instant sentDatetime;
  private Instant deliveredDatetime;
  private String callbackData;

  private String personalizedMessage;

  private String failureReason;
}
