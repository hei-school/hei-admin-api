package school.hei.haapi.model;

import static jakarta.persistence.GenerationType.IDENTITY;
import static java.util.Optional.empty;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L1;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L2;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L3;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.M1;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.M2;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.model.promotion.PromotionLevelOutOfRange;

@Entity
@Table(name = "\"promotion\"")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Promotion {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  @CreationTimestamp private Instant creationDatetime;
  private String ref;
  private String name;

  private Instant startDatetime;

  @OneToMany(mappedBy = "promotion")
  private List<Group> groups;

  public StudentLevel getLevelAt(Instant levelInstant) {
    int firstYear = startDatetime.atZone(ZoneId.systemDefault()).getYear();

    LocalDate date = levelInstant.atZone(ZoneId.systemDefault()).toLocalDate();
    int year = date.getYear();
    int month = date.getMonthValue();
    int scholarYear = (month >= 11) ? year : year - 1;
    int yearOfStudying = scholarYear - firstYear;

    return switch (yearOfStudying) {
      case 0 -> L1;
      case 1 -> L2;
      case 2 -> L3;
      case 3 -> M1;
      case 4 -> M2;
      default -> throw new PromotionLevelOutOfRange(yearOfStudying);
    };
  }

  public Optional<String> getLevelStringAt(Instant from) {
    return findLevelAt(from).map(Promotion::getLevelString);
  }

  public static String getLevelString(StudentLevel level) {
    return switch (level) {
      case L1 -> "Première";
      case L2 -> "Deuxième";
      case L3 -> "Troisième";
      case M1 -> "Quatrième";
      case M2 -> "Cinquième";
    };
  }

  public Optional<StudentLevel> findLevelAt(Instant levelInstant) {
    try {
      return Optional.of(getLevelAt(levelInstant));
    } catch (PromotionLevelOutOfRange e) {
      return empty();
    }
  }

  public String getPromotionYearString(StudentLevel level) {
    int promotionEntranceYear = getStartDatetime().atOffset(ZoneOffset.of("+3")).getYear();
    int endYear = promotionEntranceYear + 1;
    String yearStringFormat = "%d - %d";

    return switch (level) {
      case L1 -> String.format(yearStringFormat, promotionEntranceYear, endYear);
      case L2 -> String.format(yearStringFormat, promotionEntranceYear + 1, endYear + 1);
      case L3 -> String.format(yearStringFormat, promotionEntranceYear + 2, endYear + 2);
      case M1 -> String.format(yearStringFormat, promotionEntranceYear + 3, endYear + 3);
      case M2 -> String.format(yearStringFormat, promotionEntranceYear + 4, endYear + 4);
    };
  }
}
