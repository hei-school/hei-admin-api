package school.hei.haapi.model;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static org.hibernate.type.SqlTypes.NAMED_ENUM;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
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
import school.hei.haapi.endpoint.rest.model.LetterStatus;

@Entity
@Table(name = "\"letter\"")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Letter {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  @JdbcTypeCode(NAMED_ENUM)
  @Enumerated(STRING)
  private LetterStatus status;

  private String description;

  @CreationTimestamp private Instant creationDatetime;

  private Instant approvalDatetime;

  @ManyToOne
  @JoinColumn(name = "student_id", nullable = false)
  // student_id in the database, user here
  private User user;

  @Column(unique = true)
  private String ref;

  private String filePath;

  private String reasonForRefusal;

  private Integer amount;

  @ManyToOne
  @JoinColumn(name = "event_participant_id")
  private EventParticipant eventParticipant;

  @OneToOne
  @JoinColumn(name = "fee_id")
  private Fee fee;
}
