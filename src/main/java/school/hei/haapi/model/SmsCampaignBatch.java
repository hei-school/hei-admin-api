package school.hei.haapi.model;

import jakarta.persistence.Entity;
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

@Table(name = "sms_campaign_batch")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
public class SmsCampaignBatch implements Serializable {
  @Id private String id;

  @ManyToOne
  @JoinColumn(name = "campaign_id")
  private SmsCampaign campaign;

  private String befianaBatchId;

  private Instant sendAt;

  private int recipientCount;

  @CreationTimestamp private Instant createdAt;
}
