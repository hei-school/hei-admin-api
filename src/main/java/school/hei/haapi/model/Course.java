package school.hei.haapi.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.hibernate.annotations.TypeDef;

import java.io.Serializable;
import java.util.List;

import school.hei.haapi.repository.types.PostgresEnumType;

@Entity
@Table(name = "\"course\"")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TypeDef(name = "pgsql_enum", typeClass = PostgresEnumType.class)
public class Course implements Serializable {
    @Id
    private String idCourse;

    private String code;

    private String name;

    private Integer credits;

    private Integer totalHours;

    @ManyToOne
    @JoinColumn(name = "id_teacher")
    private User teacher;
}
