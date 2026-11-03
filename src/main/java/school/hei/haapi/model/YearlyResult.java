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
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import school.hei.haapi.endpoint.rest.model.StudentLevel;

@Entity
@Table(name = "\"yearly_result\"")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class YearlyResult {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "student_id", nullable = false)
  private User student;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "group_id", nullable = false)
  private Group group;

  @Enumerated(EnumType.STRING)
  @Column(name = "\"level\"", columnDefinition = "student_level", nullable = false)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  private StudentLevel level;

  @Column(name = "weighted_average")
  private BigDecimal weightedAverage;

  @Column(name = "obtained_credits")
  private BigDecimal obtainedCredits;

  @Column(name = "total_credits")
  private BigDecimal totalCredits;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "result_overview_status", nullable = false)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  private ResultOverviewStatus status;
}
