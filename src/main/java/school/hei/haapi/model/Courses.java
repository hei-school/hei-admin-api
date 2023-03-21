package school.hei.haapi.model;

import lombok.*;
import org.hibernate.annotations.TypeDef;
import school.hei.haapi.repository.types.PostgresEnumType;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "\"courses\"")
@Getter
@Setter
@TypeDef(name = "pgsql_enum", typeClass = PostgresEnumType.class)
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Courses implements Serializable {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private String id;

    @ManyToMany
    @JoinColumn(name = "student_id")
    private List<User> students;

    @OneToOne
    @JoinColumn(name = "main_teacher_id")
    private User mainTeacher;

    private String code;
    private String name;
    private int credits;
    private int totalHours;

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
