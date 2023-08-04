package school.hei.haapi.model;

import java.io.Serializable;
import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "\"exam\"")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Exam implements Serializable {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private String id;
  private Integer coefficient;
  private String title;
  @ManyToOne
  @JoinColumn(name = "course_id")
  private Course course;



  @Column(name = "examination_date", nullable = false)
  private Instant examinationDate;
}

