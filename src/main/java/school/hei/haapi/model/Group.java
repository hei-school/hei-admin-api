package school.hei.haapi.model;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static school.hei.haapi.model.fee.StudentGrade.L1;
import static school.hei.haapi.model.fee.StudentGrade.L2;
import static school.hei.haapi.model.fee.StudentGrade.L3;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;
import school.hei.haapi.model.fee.StudentGrade;

@Entity
@Table(name = "\"group\"")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Group implements Serializable {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  private String name;
  private String ref;
  private String attributedColor;

  @CreationTimestamp private Instant creationDatetime;

  @OneToMany(mappedBy = "group", fetch = LAZY)
  @ToString.Exclude
  private List<AwardedCourse> awardedCourse;

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

  public StudentGrade getStudentGradeAtInstant(LocalDate localDate) {
    int promotionYearAge =
        Period.between(LocalDate.from(this.getPromotion().getStartDatetime()), localDate)
            .getYears();
    return switch (promotionYearAge) {
      case 0 -> L1;
      case 1 -> L2;
      case 2 -> L3;
      default ->
          throw new IllegalArgumentException(
              "Invalid promotion at instant '%s': the group with ID '%s' cannot have a valid promotion for year %d at this time"
                  .formatted(localDate, this.id, promotionYearAge));
    };
  }
}
