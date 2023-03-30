package school.hei.haapi.model;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "\"interest_history\"")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class InterestHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    @ManyToOne
    @JoinColumn(name = "fee_id",nullable = false)
    private Fee fee;
    private int interestRate;
    private int interestTimeRate;
    private LocalDate interestStart;
    private LocalDate interestEnd;
    private boolean isActive;

}
