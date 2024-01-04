package school.hei.haapi.model;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import java.time.Instant;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "\"predefined_fee_type\"")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FeeType {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private String id;

    private String name;

    private Instant creationDatetime;

    private Integer totalAmount;

    private Integer numberOfMonths;

}
