package school.hei.haapi.model;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.time.Instant;
import java.util.List;

@Entity
public class Credit {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  private int value;

  private Instant creationDatetime;

  @OneToMany(mappedBy = "credit", fetch = FetchType.LAZY)
  private List<Transaction> transactions;
}
