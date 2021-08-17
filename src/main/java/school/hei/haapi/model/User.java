package school.hei.haapi.model;

import static javax.persistence.GenerationType.IDENTITY;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "\"user\"")
@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  private String firstName;
  private String lastName;
  private String email;

  @Column(name = "\"role\"")
  private String role;
}
