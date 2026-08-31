package school.hei.haapi.model;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static java.util.Optional.empty;
import static org.hibernate.type.SqlTypes.NAMED_ENUM;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
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
import org.hibernate.annotations.JdbcTypeCode;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.model.promotion.PromotionLevelOutOfRangeException;

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

  @Enumerated(STRING)
  @JdbcTypeCode(NAMED_ENUM)
  @Column(name = "cycle_level", columnDefinition = "cycle_level_enum", nullable = false)
  private CycleLevel cycleLevel;

  @OneToMany(mappedBy = "promotion")
  private List<Group> groups;

  // TODO: take into account that yearly result plays the most significant role getLevelAt too.
  public StudentLevel getLevelAt(Instant levelInstant) throws PromotionLevelOutOfRangeException {
    if (cycleLevel.getLevels().isEmpty()) {
      throw new PromotionLevelOutOfRangeException();
    }

    int firstYear = startDatetime.atZone(ZoneId.systemDefault()).getYear();
    LocalDate date = levelInstant.atZone(ZoneId.systemDefault()).toLocalDate();
    int year = date.getYear();
    int month = date.getMonthValue();
    int scholarYear = (month >= 11) ? year : year - 1;
    int yearOfStudying = scholarYear - firstYear;

    if (yearOfStudying < 0 || yearOfStudying >= cycleLevel.getLevels().size()) {
      throw new PromotionLevelOutOfRangeException(yearOfStudying);
    }
    return cycleLevel.getLevels().get(yearOfStudying);
  }

  // TODO: Going from L1 to L2 and so on is not automatic: consider repeaters please ?
  public Optional<String> getLevelStringAt(Instant from) {
    return findLevelAt(from).map(Promotion::getLevelString);
  }

  public static String getLevelString(StudentLevel level) {
    return switch (level) {
      case L1 -> "Première année de Licence";
      case L2 -> "Deuxième année de Licence";
      case L3 -> "Troisième année de Licence";
      case M1 -> "Première année de Master";
      case M2 -> "Deuxième année de Master";
    };
  }

  public Optional<StudentLevel> findLevelAt(Instant levelInstant) {
    try {
      return Optional.of(getLevelAt(levelInstant));
    } catch (PromotionLevelOutOfRangeException e) {
      return empty();
    }
  }

  public boolean hasLevelDuring(StudentLevel level, Instant periodStart, Instant periodEnd) {
    var levels = cycleLevel.getLevels();
    var levelIndex = levels.indexOf(level);
    if (levelIndex < 0) {
      return false;
    }
    var firstYear = startDatetime.atZone(ZoneId.systemDefault()).getYear();
    var windowStart = academicYearStart(firstYear + levelIndex);
    var windowEnd = academicYearStart(firstYear + levelIndex + 1);
    return periodStart.isBefore(windowEnd) && periodEnd.isAfter(windowStart);
  }

  private static Instant academicYearStart(int scholarYear) {
    return LocalDate.of(scholarYear, 11, 1).atStartOfDay(ZoneId.systemDefault()).toInstant();
  }

  public String getPromotionYearString(StudentLevel level) {
    var cycleLevels = this.getCycleLevel().getLevels();
    if (!cycleLevels.contains(level)) {
      throw new IllegalArgumentException("Level is not part of the cycle level");
    }

    int promotionEntranceYear = getStartDatetime().atOffset(ZoneOffset.of("+3")).getYear();
    int endYear = promotionEntranceYear + 1;
    String yearStringFormat = "%d - %d";

    return switch (level) {
      case L1, M1 -> String.format(yearStringFormat, promotionEntranceYear, endYear);
      case L2, M2 -> String.format(yearStringFormat, promotionEntranceYear + 1, endYear + 1);
      case L3 -> String.format(yearStringFormat, promotionEntranceYear + 2, endYear + 2);
    };
  }
}
