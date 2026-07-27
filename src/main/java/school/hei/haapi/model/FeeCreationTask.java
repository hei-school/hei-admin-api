package school.hei.haapi.model;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.EAGER;
import static jakarta.persistence.GenerationType.IDENTITY;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.*;

@Entity
@Table(name = "\"fee_creation_task\"")
@Getter
@Setter
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class FeeCreationTask {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  @ManyToOne
  @JoinColumn(name = "id_fee_creation_job")
  @ToString.Exclude
  private FeeCreationJob job;

  private String studentRef;

  private String message;

  @OneToMany(mappedBy = "task", cascade = ALL, fetch = EAGER)
  @ToString.Exclude
  @JsonIgnore
  private List<TaskStatus> statuses;

  public Optional<TaskStatus> getActualStatus() {
    return statuses == null
        ? Optional.empty()
        : statuses.stream().max(comparing(Status::getCreationDatetime, nullsFirst(naturalOrder())));
  }

  public void addStatus(TaskStatus status) {
    status.setTask(this);
    if (statuses == null) {
      statuses = new ArrayList<>();
    }
    statuses.add(status);
  }
}
