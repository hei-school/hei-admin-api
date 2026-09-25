package school.hei.haapi.model;

import static jakarta.persistence.EnumType.STRING;
import static org.hibernate.type.SqlTypes.NAMED_ENUM;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;

@Table(name = "sms_campaign")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
public class SmsCampaign implements Serializable {
  @Id private String id;

  private String message;

  @Enumerated(STRING)
  @JdbcTypeCode(NAMED_ENUM)
  private SmsCampaignStatus status;

  private String failureReason;

  @ManyToMany
  @JoinTable(
      name = "sms_campaign_contact_group",
      joinColumns = @JoinColumn(name = "campaign_id"),
      inverseJoinColumns = @JoinColumn(name = "contact_group_id"))
  @Builder.Default
  private List<SmsContactGroup> contactGroups = new ArrayList<>();

  @ManyToMany
  @JoinTable(
      name = "sms_campaign_contact",
      joinColumns = @JoinColumn(name = "campaign_id"),
      inverseJoinColumns = @JoinColumn(name = "contact_id"))
  @Builder.Default
  private List<SmsContact> contacts = new ArrayList<>();

  @ElementCollection
  @CollectionTable(
      name = "sms_campaign_manual_phone_number",
      joinColumns = @JoinColumn(name = "campaign_id"))
  @Column(name = "phone_number")
  @Builder.Default
  private List<String> manualPhoneNumbers = new ArrayList<>();

  @Builder.Default private int fileImportCount = 0;

  /** Internal-only: where the raw uploaded file was stored, for audit — not exposed via REST. */
  private String fileBucketKey;

  @Builder.Default private int recipientCount = 0;
  @Builder.Default private int recipientsRejectedForBalance = 0;
  @Builder.Default private int deliveredCount = 0;
  @Builder.Default private int failedCount = 0;

  private Integer smsSegmentsEach;
  private Integer creditsDebited;

  @ManyToOne
  @JoinColumn(name = "created_by_id")
  private User createdBy;

  @CreationTimestamp private Instant creationDatetime;
}
