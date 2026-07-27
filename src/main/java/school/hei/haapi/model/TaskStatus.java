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
@Table(name = "\"fee_creation_task_status\"")
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
public class TaskStatus extends Status {
  @ManyToOne
  @JoinColumn(name = "id_fee_creation_task")
  @ToString.Exclude
  @JsonIgnore
  private FeeCreationTask task;

  @Builder
  public TaskStatus(
      String id,
      school.hei.haapi.endpoint.rest.model.JobProgression progression,
      school.hei.haapi.endpoint.rest.model.JobHealth health,
      java.time.Instant creationDatetime,
      FeeCreationTask task) {
    super(id, progression, health, creationDatetime);
    this.task = task;
  }
}
