package school.hei.haapi.model;

import static jakarta.persistence.EnumType.STRING;
import static org.hibernate.type.SqlTypes.NAMED_ENUM;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
  @Id private String id; // make sure cor_comment.id is the PK

  @ManyToOne
  @JoinColumn(name = "cor_id")
  private Cor cor;

  @Column(name = "status")
  @Enumerated(STRING)
  @JdbcTypeCode(NAMED_ENUM)
  private CorStatus status;

  @Column(name = "creation_datetime")
  private Instant creationDatetime;

  // getters and setters
}
