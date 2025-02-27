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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import school.hei.haapi.model.fee.PaymentType;

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

  private Long firstGradeCount;
  private Long secondGradeCount;
  private Long thirdGradeCount;

  private Long remedialFeesCount;
  private Long workStudyCount;

  private Long monthlyCount;
  private Long yearlyCount;

  private Long bankTransferCount;
  private Long mpbsCount;

  @CreationTimestamp private Instant insertDatetime;

  public enum AdvancedFeeStatsType {
    TOTAL_COUNT,
    PAID_COUNT,
    LATE_COUNT,
    PENDING_COUNT
  }
}
