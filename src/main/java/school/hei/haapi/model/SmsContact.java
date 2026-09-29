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

@Table(name = "sms_contact")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
public class SmsContact implements Serializable {
  @Id private String id;

  private String phoneNumber;
  private String name;

  @ManyToOne
  @JoinColumn(name = "owner_id")
  private User owner;

  @Enumerated(STRING)
  @JdbcTypeCode(NAMED_ENUM)
  private SmsContactOwnerRole ownerRole;

  @Builder.Default private boolean isDeleted = false;

  @CreationTimestamp private Instant creationDatetime;
}
