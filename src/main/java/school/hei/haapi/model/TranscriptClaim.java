package school.hei.haapi.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import school.hei.haapi.repository.types.PostgresEnumType;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.Instant;

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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    @CreationTimestamp
    private Instant creationDate;
    private Instant closedDate;
    private String reason;
    @Type(type = "pgsql_enum")
    @Enumerated(value = EnumType.STRING)
    private ClaimStatus claimStatus;
    @ManyToOne
    @JoinColumn(name = "transcript_version_id", nullable = false)
    private TranscriptVersion transcriptVersion;

    public enum ClaimStatus{
        OPEN, CLOSE
    }
}
