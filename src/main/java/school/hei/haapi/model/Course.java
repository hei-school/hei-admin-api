package school.hei.haapi.model;

import lombok.*;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "\"user\"")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Course implements Serializable {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private String id;

    @NotBlank(message = "Code is mandatory")
    private String code;

    @NotBlank(message = "Name is mandatory")
    private String name;

    @NotBlank(message = "Credits name is mandatory")
    private int credits;

    @NotBlank(message = "Credits name is mandatory")
    private int total_hours;

    @OneToMany
    @JoinColumn(name = "main_teacher")
    private User main_teacher;
}
