package school.hei.haapi.model;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "\"retake_exam_session\"")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class RetakeExamSession implements Serializable {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  private String title;
  private Instant dateFrom;
  private Instant dateTo;

  @OneToMany(mappedBy = "retakeExamSession")
  private List<RetakeExam> retakeExams = new ArrayList<>();
}
