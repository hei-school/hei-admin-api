package school.hei.haapi.model;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static org.hibernate.type.SqlTypes.NAMED_ENUM;

import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;

@EqualsAndHashCode
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Setter
@MappedSuperclass
@SuperBuilder(toBuilder = true)
public abstract class CorCommentBase {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  protected String id;

  @CreationTimestamp Instant creationDatetime;

  @Enumerated(STRING)
  @JdbcTypeCode(NAMED_ENUM)
  protected CorStatus status;

  protected String comment;

  @ManyToOne
  @JoinColumn(name = "cor_id", updatable = false)
  protected Cor cor;
}
