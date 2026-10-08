package school.hei.haapi.model;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "\"student_badge\"")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder(toBuilder = true)
public class StudentBadge implements Serializable {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  @ManyToOne
  @JoinColumn(name = "student_id", referencedColumnName = "id")
  @ToString.Exclude
  private User student;

  private String publicId;

  @CreationTimestamp private Instant creationDatetime;

  private Instant revocationDatetime;

  private String academicYear;

  private Instant expirationDatetime;

  public boolean isRevoked() {
    return revocationDatetime != null;
  }

  public boolean isExpiredAt(Instant instant) {
    return expirationDatetime != null && !instant.isBefore(expirationDatetime);
  }

  public boolean isValidAt(Instant instant) {
    return !isRevoked() && !isExpiredAt(instant);
  }
}
