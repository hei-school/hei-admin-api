package school.hei.haapi.model;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static org.hibernate.type.SqlTypes.NAMED_ENUM;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import school.hei.haapi.endpoint.rest.model.Scope;

@Entity
@Table(name = "\"announcement\"")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Announcement {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  @JdbcTypeCode(NAMED_ENUM)
  @Enumerated(STRING)
  private Scope scope;

  private String title;

  private String content;

  @ManyToOne private User author;

  @CreationTimestamp private Instant creationDatetime;

  @ManyToMany
  @JoinTable(
      name = "announcement_target",
      joinColumns = @JoinColumn(name = "announcement_id"),
      inverseJoinColumns = @JoinColumn(name = "group_id"))
  private List<Group> groups;
}
