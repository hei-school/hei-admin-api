package school.hei.haapi.model;

import static jakarta.persistence.GenerationType.IDENTITY;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "\"fee_creation_job_statistics\"")
@Getter
@Setter
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class FeeCreationJobStatistics {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  @OneToOne
  @JoinColumn(name = "id_fee_creation_job")
  @ToString.Exclude
  @JsonIgnore
  private FeeCreationJob job;

  private Integer totalCount;

  private Integer successCount;

  private Integer failureCount;

  @UpdateTimestamp private Instant updateDatetime;
}
