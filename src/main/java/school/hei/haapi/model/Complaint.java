package school.hei.haapi.model;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.EAGER;
import static jakarta.persistence.GenerationType.IDENTITY;
import static org.hibernate.type.SqlTypes.NAMED_ENUM;
import static school.hei.haapi.model.ComplaintStatus.CREATED;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;

@Entity
@Table(name = "\"complaint\"")
@Getter
@Setter
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Complaint {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  @JdbcTypeCode(NAMED_ENUM)
  @Enumerated(STRING)
  @Column(nullable = false)
  private ComplaintType type;

  private String title;

  private String description;

  @Builder.Default
  @JdbcTypeCode(NAMED_ENUM)
  @Enumerated(STRING)
  @Column(nullable = false)
  private ComplaintStatus status = CREATED;

  @ManyToOne
  @JoinColumn(name = "student_id", updatable = false)
  private User student;

  @EqualsAndHashCode.Exclude @CreationTimestamp private Instant createdAt;

  @EqualsAndHashCode.Exclude private Instant reminderSentAt;

  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  @OneToMany(mappedBy = "complaint", fetch = EAGER)
  private List<ComplaintAssignee> assignees;

  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  @OneToMany(mappedBy = "complaint", fetch = EAGER)
  private List<ComplaintStatusHistory> statusHistory;

  public Optional<User> getRoutedManager() {
    if (assignees == null) {
      return Optional.empty();
    }
    return assignees.stream()
        .filter(assignee -> assignee.getAssignedBy() == null)
        .map(ComplaintAssignee::getUser)
        .findFirst();
  }

  public Optional<ComplaintStatusHistory> getLastStatusHistory() {
    if (statusHistory == null) {
      return Optional.empty();
    }
    return statusHistory.stream().max(Comparator.comparing(ComplaintStatusHistory::getDatetime));
  }
}
