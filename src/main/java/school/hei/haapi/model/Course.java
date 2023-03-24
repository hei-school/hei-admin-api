package school.hei.haapi.model;

import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.TypeDef;
import school.hei.haapi.repository.types.PostgresEnumType;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

import static javax.persistence.GenerationType.IDENTITY;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
@Entity
@TypeDef(name = "pgsql_enum", typeClass = PostgresEnumType.class)
@Table(name = "\"course\"")

public class Course implements Serializable {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private String id;

    private String code;

    private String name;

    private int credits;

    private int totalHours;

    @OneToOne
    @JoinColumn(name = "main_teacher", nullable = false, referencedColumnName = "id")
    private User mainTeacher;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        Course course = (Course) o;
        return id != null && Objects.equals(id, course.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
