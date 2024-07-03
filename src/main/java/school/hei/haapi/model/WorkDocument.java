package school.hei.haapi.model;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.*;
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
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Table(name = "\"work_document\"")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
@SQLDelete(sql = "update \"work_document\" set is_deleted = true where id = ?")
@Where(clause = "is_deleted = false")
public class WorkDocument implements Serializable {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  private String filename;

  @CreationTimestamp private Instant creationDatetime;

  private Instant commitmentBegin;

  private Instant commitmentEnd;

  private String filePath;

  @ManyToOne
  @JoinColumn(name = "student_id")
  private User student;

  private boolean isDeleted;
}
