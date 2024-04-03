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

@Table(name = "\"work_file\"")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class WorkFile implements Serializable {
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
}
