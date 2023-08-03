package school.hei.haapi.model;

import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import school.hei.haapi.repository.types.PostgresEnumType;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;

import static javax.persistence.FetchType.LAZY;

@Entity
@Table(name = "\"student_transcript_claim\"")
@Getter
@Setter
@ToString
@TypeDef(name = "pgsql_enum", typeClass = PostgresEnumType.class)
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class StudentTranscriptClaim implements Serializable {
  @Id
  private String id;

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "transcript_id")
  private Transcript transcript;

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "student_transcript_version_id")
  private StudentTranscriptVersion transcriptVersion;

  @Type(type = "pgsql_enum")
  @Enumerated(EnumType.STRING)
  private school.hei.haapi.endpoint.rest.model.StudentTranscriptClaim.StatusEnum status;

  private Instant creationDatetime;

  private Instant closedDatetime;

  private String reason;

  public static final String CREATION_DATETIME = "creationDatetime";
}
