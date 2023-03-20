package school.hei.haapi.model;

import java.io.Serializable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
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

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "\"course\"")
@Getter
@Setter
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Course implements Serializable {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;
  private String code;
  private String name;
  private int credits;
  private int totalHours;
  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "user_id", nullable = false)
  private User mainTeacher;
}
