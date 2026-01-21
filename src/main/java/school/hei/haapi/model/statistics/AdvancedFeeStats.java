package school.hei.haapi.model.statistics;

import static jakarta.persistence.EnumType.STRING;
import static org.hibernate.type.SqlTypes.NAMED_ENUM;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
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
import org.hibernate.annotations.UpdateTimestamp;

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
  @Id private String id;

  @JdbcTypeCode(NAMED_ENUM)
  @Enumerated(STRING)
  private AdvancedFeeStatsType statType;

  private long firstGradeCount;
  private long secondGradeCount;
  private long thirdGradeCount;
  private long unknownGradeCount;

  private long remedialFeesCount;
  private long workStudyCount;

  private long monthlyCount;
  private long yearlyCount;
  private long unknownFrequencyCount;

  private Long bankTransferCount;
  private Long mpbsCount;

  @CreationTimestamp private Instant creationDatetime;
  @UpdateTimestamp private Instant updateDatetime;

  private LocalDate statStartDate;
  private LocalDate statEndDate;

  @JdbcTypeCode(NAMED_ENUM)
  @Enumerated(STRING)
  private AdvancedFeeStatsCountType countType;

  public enum AdvancedFeeStatsType {
    TOTAL_COUNT,
    PAID_COUNT,
    LATE_COUNT,
    UNPAID_COUNT,
    PENDING_COUNT
  }

  public enum AdvancedFeeStatsCountType {
    ACCOUNTING,
    RECEIPT;
  }
}
