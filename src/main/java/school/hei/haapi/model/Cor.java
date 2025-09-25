package school.hei.haapi.model;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static org.hibernate.type.SqlTypes.NAMED_ENUM;
import static school.hei.haapi.model.CorComment.CorStatus.IN_PROGRESS;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import school.hei.haapi.model.CorComment.CorStatus;

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
  @JoinColumn(name = "concerned_student_id", updatable = false)
  private User concernedStudent;

  @EqualsAndHashCode.Exclude @CreationTimestamp private Instant creationDatetime;

  private Instant interviewDatetime;

  @OneToMany(mappedBy = "cor")
  @EqualsAndHashCode.Exclude
  private List<CorComment> comments;

  @Enumerated(STRING)
  @JdbcTypeCode(NAMED_ENUM)
  @Builder.Default
  @Column(nullable = false)
  private CorStatus status = IN_PROGRESS;

  public void addComment(CorComment comment) {
    comments.add(comment);
    status = comment.getStatus();
  }
}
