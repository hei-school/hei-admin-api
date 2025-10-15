package school.hei.haapi.model;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.EAGER;
import static jakarta.persistence.GenerationType.IDENTITY;
import static org.hibernate.type.SqlTypes.NAMED_ENUM;
import static school.hei.haapi.model.CorStatus.IN_PROGRESS;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;

@Entity
@Table(name = "\"cor\"")
@Getter
@Setter
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
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

  @ToString.Exclude
  @ManyToMany(fetch = EAGER)
  @EqualsAndHashCode.Exclude
  @JoinTable(
      name = "cor_interviewers",
      joinColumns = @JoinColumn(name = "cor_id"),
      inverseJoinColumns = @JoinColumn(name = "user_id"))
  private List<User> interviewers;

  public Optional<CorComment> getLastComment() {
    if (comments == null) return Optional.empty();
    return comments.stream().max(Comparator.comparing(CorComment::getCreationDatetime));
  }

  @Builder.Default
  @Enumerated(STRING)
  @JdbcTypeCode(NAMED_ENUM)
  @Column(nullable = false)
  private CorStatus status = IN_PROGRESS;
}
