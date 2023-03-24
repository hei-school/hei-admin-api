package school.hei.haapi.model;

import lombok.*;
import org.hibernate.annotations.TypeDef;
import school.hei.haapi.repository.types.PostgresEnumType;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "\"course\"")
@TypeDef(name = "pgsql_enum", typeClass = PostgresEnumType.class)
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Courses implements Serializable {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private String id;

    @NotBlank(message = "Code is mandatory")
    private String code;

    @NotBlank(message = "Name is mandatory")
    private String name;

    @NotBlank(message = "Credits name is mandatory")
    private int credits;
    @NotBlank(message = "total-hours name is mandatory")
    private Integer total_hours;

    @OneToMany
    @JoinColumn(name = "main_teacher_id")
    private User main_teacher;
    @ManyToMany
    @JoinTable(
            name= "linked_or_unliked",
            joinColumns=@JoinColumn(name = "User_id"),
            inverseJoinColumns=@JoinColumn(name = "course_id")
    )
    private List<User> userStatus;
}