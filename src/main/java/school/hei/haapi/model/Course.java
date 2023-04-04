package school.hei.haapi.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "\"course\"")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Course {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  private String code;

  private String name;

  private Integer credits;

  private int totalHours;

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "main_teacher")
  private User mainTeacher;

}
