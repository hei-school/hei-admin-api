package school.hei.haapi.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

@AllArgsConstructor
@Data
@Table(name="\"event\"")
@NoArgsConstructor
@Entity
@EqualsAndHashCode
public class Event implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private String id;
  @NotBlank
  @Column(name = "event_type" , nullable = false )
  private String eventType;
  @NotBlank(message = "must not be blank")
  @NotEmpty
  private String startTime;
  @NotBlank
  @NotEmpty
  private String endTime;

}
