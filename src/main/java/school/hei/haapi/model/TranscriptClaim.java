package school.hei.haapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Entity;
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
@EqualsAndHashCode
@ToString
@Builder(toBuilder = true)
public class TranscriptClaim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    private Instant creationDate;
    private Instant closedDate;
    private String reason;
    private ClaimStatus status;
    @ManyToOne
    @JoinColumn(name = "transcript_id", nullable = false)
    private TranscriptVersion transcriptVersion;

    public enum ClaimStatus{
        OPEN, CLOSE
    }
}
