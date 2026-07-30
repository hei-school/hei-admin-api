package school.hei.haapi.model;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
public class Credit {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  @OneToOne
  @JoinColumn(name = "student_id", nullable = false, updatable = false)
  private User student;

  private int amount;

  private Instant creationDatetime;

  @OneToMany(mappedBy = "credit", fetch = FetchType.LAZY)
  private List<Transaction> transactions;
}
