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
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;

@Table(name = "notification")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
public class Notification implements Serializable {
  @Id private String id;

  @ManyToOne
  @JoinColumn(name = "recipient_id")
  private User recipient;

  private String subject;
  private String body;

  @Builder.Default private boolean read = false;

  @Enumerated(STRING)
  @JdbcTypeCode(NAMED_ENUM)
  private NotificationResolutionStatus resolutionStatus;

  @ManyToOne
  @JoinColumn(name = "sms_campaign_id")
  private SmsCampaign smsCampaign;

  @CreationTimestamp private Instant creationDatetime;
}
