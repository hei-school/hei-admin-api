package school.hei.haapi.model;

import java.time.Instant;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import school.hei.haapi.repository.types.PostgresEnumType;

@Entity
@Table(name = "\"transcript_claim\"")
@Data
@AllArgsConstructor
@NoArgsConstructor
@TypeDef(name = "pgsql_enum", typeClass = PostgresEnumType.class)
@EqualsAndHashCode
@ToString
@Builder(toBuilder = true)
public class TranscriptClaim {

  @Id private String id;
  @CreationTimestamp private Instant creationDatetime;
  private Instant closedDatetime;
  private String reason;

  @Type(type = "pgsql_enum")
  @Enumerated(value = EnumType.STRING)
  private ClaimStatus claimStatus;

  @ManyToOne
  @JoinColumn(name = "transcript_version_id", nullable = false)
  private TranscriptVersion transcriptVersion;

  public enum ClaimStatus {
    OPEN,
    CLOSE
  }
}
