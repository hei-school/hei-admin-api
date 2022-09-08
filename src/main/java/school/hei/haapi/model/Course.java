package school.hei.haapi.model;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
@Table(name= "\"course\"")
public class Course implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @Column(nullable = false,unique = true)
    private String ref;

    @NotBlank(message = "Name cannot be blank")
    private String name;

    @Min(value = 1)
    private int credits;

    @Min(value = 1)
    private int total_hours;
}
