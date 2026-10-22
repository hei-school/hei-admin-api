package school.hei.haapi.model;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static org.hibernate.type.SqlTypes.NAMED_ENUM;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;

@Entity
@Table(name = "\"complaint_status_history\"")
@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class ComplaintStatusHistory {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  @CreationTimestamp private Instant datetime;

  @JdbcTypeCode(NAMED_ENUM)
  @Enumerated(STRING)
  private ComplaintStatus status;

  private String comment;

  @ManyToOne
  @JoinColumn(name = "complaint_id")
  @ToString.Exclude
  @JsonIgnore
  private Complaint complaint;

  @ManyToOne
  @JoinColumn(name = "changed_by_id")
  private User changedBy;
}
