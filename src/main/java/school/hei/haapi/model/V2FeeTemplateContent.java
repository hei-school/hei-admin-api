package school.hei.haapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigInteger;
import java.time.LocalDate;
import lombok.*;

@Entity
@Table(name = "\"v2_fee_template_content\"")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class V2FeeTemplateContent {
  @Id private String id;

  @ManyToOne
  @JoinColumn(name = "id_fee_template")
  @JsonIgnore
  private V2FeeTemplate feeTemplate;

  private String label;

  private BigInteger amount;

  private LocalDate dueDate;
}
