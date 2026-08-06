package school.hei.haapi.model;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static org.hibernate.type.SqlTypes.NAMED_ENUM;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import school.hei.haapi.endpoint.rest.model.StudentLevel;

@Table(name = "documenso_document")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
public class DocumensoDocument implements Serializable {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  private Long documensoDocumentId;

  @ManyToOne
  @JoinColumn(name = "documenso_template_id")
  private TemplateDocumenso template;

  @ManyToOne
  @JoinColumn(name = "promotion_id")
  private Promotion promotion;

  @Enumerated(STRING)
  private StudentLevel level;

  @Enumerated(STRING)
  @JdbcTypeCode(NAMED_ENUM)
  private Status status;

  @ManyToOne
  @JoinColumn(name = "file_info_id")
  private FileInfo fileInfo;

  @CreationTimestamp private Instant creationDatetime;

  private Instant completedDatetime;

  public enum Status {
    PENDING,
    COMPLETED,
    REJECTED,
  }
}
