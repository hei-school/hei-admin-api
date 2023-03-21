package school.hei.haapi.model;

import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import school.hei.haapi.repository.types.PostgresEnumType;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "\"course_student\"")
@TypeDef(name = "pgsql_enum", typeClass = PostgresEnumType.class)
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourseStudent {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private String id;
    @ManyToOne
    @MapsId("courseId")
    @JoinColumn(name = "course_id")
    private Course course;
    @ManyToOne
    @MapsId("studentId")
    @JoinColumn(name = "student_id")
    private User student;
    @Type(type = "pgsql_enum")
    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Status {
        LINKED,UNLINKED
    }


}
