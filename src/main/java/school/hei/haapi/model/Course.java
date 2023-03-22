package school.hei.haapi.model;

import lombok.*;
import org.hibernate.annotations.TypeDef;
import school.hei.haapi.repository.types.PostgresEnumType;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "\"course\"")
@Getter
@Setter
@TypeDef(name = "pgsql_enum", typeClass = PostgresEnumType.class)
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Course {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private String id;
    private String code ;
    private String name;
    private Integer credits;
    private Integer totalHours;

    @ManyToOne
    @JoinColumn(name="teacher_id")
    private User mainTeacher;


}
