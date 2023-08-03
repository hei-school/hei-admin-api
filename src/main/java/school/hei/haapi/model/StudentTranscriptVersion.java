package school.hei.haapi.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "\"student_transcript_version\"")
@Getter
@Setter
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class StudentTranscriptVersion implements Serializable {
  @Id
  private String id;

  @ManyToOne
  @JoinColumn(name = "transcript_id")
  private Transcript transcript;

  private Integer ref;

  @ManyToOne
  @JoinColumn(name = "responsible_id")
  private User responsible;

  private Instant creationDatetime;

  public static final String CREATION_DATETIME = "creationDatetime";
}
