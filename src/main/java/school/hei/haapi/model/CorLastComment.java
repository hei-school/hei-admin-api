package school.hei.haapi.model;

import static jakarta.persistence.EnumType.STRING;
import static org.hibernate.type.SqlTypes.NAMED_ENUM;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;

@Entity
@Table(name = "cor_last_comment")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Immutable
public class CorLastComment {
  @Id private String id;

  Instant creationDatetime;

  @Enumerated(STRING)
  @JdbcTypeCode(NAMED_ENUM)
  private CorStatus status;

  private String comment;

  @OneToOne
  @JoinColumn(name = "cor_id", updatable = false)
  private Cor cor;
}
