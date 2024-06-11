package school.hei.haapi.model;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static org.hibernate.type.SqlTypes.NAMED_ENUM;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import school.hei.haapi.endpoint.rest.model.FileType;

@Entity
@Table(name = "\"file_info\"")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder(toBuilder = true)
@SQLDelete(sql = "update \"file_info\" set is_deleted = true where id = ?")
@Where(clause = "is_deleted = false")
public class FileInfo implements Serializable {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  @ManyToOne private User user;

  private String name;

  @CreationTimestamp private Instant creationDatetime;

  @Enumerated(STRING)
  @JdbcTypeCode(NAMED_ENUM)
  private FileType fileType;

  private String filePath;

  private boolean isDeleted;
}
