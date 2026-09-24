package school.hei.haapi.model;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

/**
 * A folder we created in Documenso, remembered by the path we built it from. Keyed by path rather
 * than by (year, level) so that the same table serves any tree we decide to lay out later.
 */
@Entity
@Table(name = "\"documenso_folder\"")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder(toBuilder = true)
public class DocumensoFolder implements Serializable {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  private String documensoFolderId;

  private String path;

  @CreationTimestamp private Instant creationDatetime;
}
