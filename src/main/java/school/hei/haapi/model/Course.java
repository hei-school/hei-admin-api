package school.hei.haapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

import java.util.List;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "\"course\"")
@Getter
@Setter
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Course {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private String id;

    @NotBlank(message = "Code is mandatory")
    private String code;

    @NotBlank(message = "Name is mandatory")
    private String name;

    private int credits;

    @Column(name = "total_hours")
    private int totalHours;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User mainTeacher;

    @ManyToMany
    @JoinTable(
            name = "have_student",
            joinColumns = @JoinColumn(name = "id_course", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "id_student", referencedColumnName = "id")
    )
    private Set<User> Student;

}
