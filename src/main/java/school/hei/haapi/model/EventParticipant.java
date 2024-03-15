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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import school.hei.haapi.endpoint.rest.model.AttendanceStatus;

@Entity
@Table(name = "\"event_participant\"")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventParticipant {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  @ManyToOne
  @JoinColumn(name = "event_id", referencedColumnName = "id")
  private Event event;

  @ManyToOne
  @JoinColumn(name = "participant_id", referencedColumnName = "id")
  private User participant;

  @JdbcTypeCode(NAMED_ENUM)
  @Enumerated(STRING)
  private AttendanceStatus status;

  @ManyToOne
  @JoinColumn(name = "actual_group_id", referencedColumnName = "id")
  private Group group;
}
