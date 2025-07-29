package school.hei.haapi.model;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "\"group\"")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@SQLDelete(sql = "update \"group\" set is_deleted = true where id = ?")
@Where(clause = "is_deleted = false")
public class Group implements Serializable {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  private String name;
  private String ref;
  private String attributedColor;

  // TODO: should replace this with PrePersist because this always sets to Instant.now() even with a
  // set value
  @EqualsAndHashCode.Exclude @CreationTimestamp private Instant creationDatetime;

  @EqualsAndHashCode.Exclude @Builder.Default private boolean isDeleted = false;

  @ManyToMany(mappedBy = "groups", fetch = LAZY)
  @ToString.Exclude
  private List<CourseAssignment> courseAssignments;

  @OneToMany(mappedBy = "group", fetch = LAZY)
  @ToString.Exclude
  private List<GroupFlow> groupFlows;

  @ManyToOne
  @JoinColumn(name = "promotion_id", referencedColumnName = "id")
  private Promotion promotion;

  @Override
  public String toString() {
    return "Group{"
        + "id='"
        + id
        + '\''
        + ", name='"
        + name
        + '\''
        + ", ref='"
        + ref
        + '\''
        + ", creationDatetime="
        + creationDatetime
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    Group user = (Group) o;
    return id != null && Objects.equals(id, user.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
