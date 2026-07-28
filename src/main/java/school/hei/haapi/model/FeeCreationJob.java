package school.hei.haapi.model;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.EAGER;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "\"fee_creation_job\"")
@Getter
@Setter
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class FeeCreationJob {
  @Id private String id;

  @ManyToOne(fetch = EAGER)
  @JoinColumn(name = "id_fee_template")
  private V2FeeTemplate feeTemplate;

  @OneToMany(mappedBy = "job", cascade = ALL, fetch = EAGER)
  @JsonIgnore
  private List<FeeCreationTask> tasks;

  @OneToMany(mappedBy = "job", cascade = ALL)
  @ToString.Exclude
  @JsonIgnore
  private List<JobStatus> statuses;

  @OneToOne(mappedBy = "job", cascade = ALL)
  @ToString.Exclude
  @JsonIgnore
  private FeeCreationJobStatistics statistics;

  @CreationTimestamp private Instant creationDatetime;

  private Instant endDatetime;

  public void addTask(FeeCreationTask task) {
    task.setJob(this);
    tasks.add(task);
  }

  public Optional<JobStatus> getActualStatus() {
    return statuses == null
        ? Optional.empty()
        : statuses.stream().max(comparing(Status::getCreationDatetime, nullsFirst(naturalOrder())));
  }
}
