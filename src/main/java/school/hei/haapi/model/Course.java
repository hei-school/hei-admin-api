package school.hei.haapi.model;

import lombok.*;
import org.hibernate.annotations.TypeDef;
import school.hei.haapi.repository.types.PostgresEnumType;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

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
public class Course implements Serializable {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private String id;

    @NotBlank(message = "Ref is mandatory")
    private String ref;

    @NotBlank(message = "name is mandatory")
    private String name;

    @NotBlank(message = "Credits is mandatory")
    private Integer credits;
    @NotBlank(message = "Total hours is mandatory")
    private Integer total_hours;

}