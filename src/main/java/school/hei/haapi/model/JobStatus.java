package school.hei.haapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "\"fee_creation_job_status\"")
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
public class JobStatus extends Status {
  @ManyToOne
  @JoinColumn(name = "id_fee_creation_job")
  @ToString.Exclude
  @JsonIgnore
  private FeeCreationJob job;

  @Builder
  public JobStatus(
      String id,
      school.hei.haapi.endpoint.rest.model.JobProgression progression,
      school.hei.haapi.endpoint.rest.model.JobHealth health,
      java.time.Instant creationDatetime,
      FeeCreationJob job) {
    super(id, progression, health, creationDatetime);
    this.job = job;
  }
}
