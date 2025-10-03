package school.hei.haapi.model;

import static jakarta.persistence.FetchType.EAGER;
import static jakarta.persistence.GenerationType.IDENTITY;
import static school.hei.haapi.model.CorStatus.IN_PROGRESS;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
@Table(name = "\"cor\"")
@Builder(toBuilder = true)
public class Cor {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  private String description;

  @ManyToOne
  @JoinColumn(name = "student_id", updatable = false)
  private User student;

  @EqualsAndHashCode.Exclude @CreationTimestamp private Instant creationDatetime;

  private Instant interviewDatetime;

  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  @OneToMany(mappedBy = "cor", fetch = EAGER)
  private List<CorComment> comments;

  @Setter(AccessLevel.NONE)
  @Getter(AccessLevel.NONE)
  @OneToOne(mappedBy = "cor")
  private CorLastComment lastComment;

  public Optional<CorComment> getLastComment() {
    if (comments == null) return Optional.empty();
    return comments.stream().max(Comparator.comparing(CorComment::getCreationDatetime));
  }

  public CorStatus getStatus() {
    return getLastComment().map(CorComment::getStatus).orElse(IN_PROGRESS);
  }
}
