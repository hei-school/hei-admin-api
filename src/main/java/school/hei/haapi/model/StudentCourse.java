package school.hei.haapi.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "\"student_course\"")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentCourse implements Serializable {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private String id;

    @ManyToOne(cascade = CascadeType.ALL)
    private Course course_id;

    @NotBlank(message = "Student is mandatory")
    @ManyToOne(cascade = CascadeType.ALL)
    private User student_id;

    @Type(type = "pgsql_enum")
    @Enumerated(EnumType.STRING)
    private Status status = Status.LINKED;


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        StudentCourse studentCourse = (StudentCourse) o;
        return id != null && Objects.equals(id, studentCourse.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    public enum Status {
        LINKED, UNLINKED
    }
}
