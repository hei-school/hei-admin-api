package school.hei.haapi.model;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "\"student_result_overview\"")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class StudentResultOverview {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "graduation_promotion_id", nullable = false)
  private Promotion graduationPromotion;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "student_id", nullable = false)
  private User student;

  @Column(name = "weighted_average", nullable = false)
  private BigDecimal weightedAverage;

  @Column(name = "obtained_credits", nullable = false)
  private BigDecimal obtainedCredits;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "result_overview_status")
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  private ResultOverviewStatus status;

  @Column(name = "total_credits", nullable = false)
  private BigDecimal totalCredits;
}
