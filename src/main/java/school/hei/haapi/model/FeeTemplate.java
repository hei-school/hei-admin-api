package school.hei.haapi.model;

import static javax.persistence.GenerationType.IDENTITY;

import java.time.Instant;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "\"fee_template\"")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FeeTemplate {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  private String name;

  @CreationTimestamp
  private Instant creationDatetime;

  private Integer totalAmount;

  private Integer numberOfMonths;
}
