package school.hei.haapi.model;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static org.hibernate.type.SqlTypes.NAMED_ENUM;

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
import org.hibernate.annotations.JdbcTypeCode;
import school.hei.haapi.endpoint.rest.model.ProfessionalExperienceFileTypeEnum;

@Table(name = "\"work_document\"")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
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

  @JdbcTypeCode(NAMED_ENUM)
  @Enumerated(STRING)
  private ProfessionalExperienceFileTypeEnum professionalExperienceType;
}
