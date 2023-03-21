package school.hei.haapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import school.hei.haapi.repository.types.PostgresEnumType;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "\"course_student\"")
@Getter
@Setter
@ToString
@TypeDef(name = "pgsql_enum", typeClass = PostgresEnumType.class)
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
