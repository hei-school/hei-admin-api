package school.hei.haapi.model.Mpbs;

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
import school.hei.haapi.endpoint.rest.model.MpbsStatus;

@Entity
@Table(name = "\"mpbs_history\"")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode
@ToString
public class MpbsStatusHistory {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  @ManyToOne
  @JoinColumn(name = "mpbs_id", nullable = false)
  private Mpbs mpbs;

  @Column(name = "\"status\"")
  @Enumerated(STRING)
  @JdbcTypeCode(NAMED_ENUM)
  private MpbsStatus status;

  @CreationTimestamp private Instant creationInstant;

  @UpdateTimestamp private Instant updateInstant;

  public static MpbsStatusHistory fromMpbs(Mpbs mpbs) {
    return MpbsStatusHistory.builder().mpbs(mpbs).status(mpbs.getStatus()).build();
  }
}
