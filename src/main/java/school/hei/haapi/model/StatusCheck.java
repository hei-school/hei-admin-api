package school.hei.haapi.model;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static org.hibernate.type.SqlTypes.NAMED_ENUM;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import school.hei.haapi.endpoint.rest.model.StatusCheckResult;

@Entity
@Table(name = "\"status_check\"")
@Getter
@Setter
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class StatusCheck {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  @ManyToOne
  @JoinColumn(name = "concerned_student_id", nullable = false, updatable = false)
  private User concernedStudent;

  @ManyToOne
  @JoinColumn(name = "requesting_user_id", nullable = false, updatable = false)
  private User requestingUser;

  @Column(nullable = false)
  private String description;

  @Builder.Default
  @Enumerated(STRING)
  @JdbcTypeCode(NAMED_ENUM)
  @Column(nullable = false)
  private StatusCheckResult result = StatusCheckResult.PENDING;

  @EqualsAndHashCode.Exclude
  @CreationTimestamp
  @Column(updatable = false)
  private Instant creationDatetime;

  @EqualsAndHashCode.Exclude @UpdateTimestamp private Instant updateDatetime;
}
