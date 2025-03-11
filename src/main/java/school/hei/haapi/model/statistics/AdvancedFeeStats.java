package school.hei.haapi.model.statistics;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static org.hibernate.type.SqlTypes.NAMED_ENUM;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
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
@Table(name = "\"stats_advanced_fees\"")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class AdvancedFeeStats {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  @JdbcTypeCode(NAMED_ENUM)
  @Enumerated(STRING)
  private AdvancedFeeStatsType statType;

  private long firstGradeCount;
  private long secondGradeCount;
  private long thirdGradeCount;

  private long remedialFeesCount;
  private long workStudyCount;

  private long monthlyCount;
  private long yearlyCount;

  private Long bankTransferCount;
  private Long mpbsCount;

  @CreationTimestamp private Instant updateDatetime;
  private LocalDate statDate;

  public enum AdvancedFeeStatsType {
    TOTAL_COUNT,
    PAID_COUNT,
    LATE_COUNT,
    PENDING_COUNT
  }

  public AdvancedFeeStats(
      Long firstGradeCount,
      Long secondGradeCount,
      Long thirdGradeCount,
      Long remedialFeesCount,
      Long workStudyCount,
      Long monthlyCount,
      Long yearlyCount,
      Long bankTransferCount,
      Long mpbsCount,
      AdvancedFeeStatsType statType) {
    this.statType = statType;
    this.firstGradeCount = firstGradeCount;
    this.secondGradeCount = secondGradeCount;
    this.thirdGradeCount = thirdGradeCount;
    this.remedialFeesCount = remedialFeesCount;
    this.workStudyCount = workStudyCount;
    this.monthlyCount = monthlyCount;
    this.yearlyCount = yearlyCount;
    this.bankTransferCount = bankTransferCount;
    this.mpbsCount = mpbsCount;
  }
}
