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
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;

@Setter
@Getter
@Entity
@Table(name = "\"announcement_reaction\"")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AnnouncementReaction {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  @ManyToOne
  @JoinColumn(name = "announcement_id", nullable = false)
  private Announcement announcement;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @UpdateTimestamp private Instant updateDateTime;

  @Column(name = "\"reaction\"")
  @Enumerated(STRING)
  @JdbcTypeCode(NAMED_ENUM)
  private ReactionEnum reaction;

  public enum ReactionEnum {
    UNCHECK,
    CHECK;
  }
}
