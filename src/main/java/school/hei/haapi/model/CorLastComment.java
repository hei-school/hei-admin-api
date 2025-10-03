package school.hei.haapi.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Entity
@Table(name = "cor_last_comment")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Immutable
public class CorLastComment {
  @Id
  @OneToOne
  @JoinColumn(name = "id", updatable = false, insertable = false)
  private CorComment comment;

  @OneToOne
  @JoinColumn(name = "cor_id", updatable = false, insertable = false)
  private Cor cor;
}
