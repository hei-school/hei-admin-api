package school.hei.haapi.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import school.hei.haapi.repository.types.PostgresEnumType;

import javax.persistence.*;
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

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User student;

    @Type(type = "pgsql_enum")
    @Enumerated(EnumType.STRING)
    private school.hei.haapi.model.Course.StatusEnum status;

    public Course id(String id) {
        this.id = id;
        return this;
    }

    public enum StatusEnum {
        LINKED("LINKED"),

        UNLINKED("UNLINKED");

        private String value;

        StatusEnum(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static Course.StatusEnum fromValue(String value) {
            for (Course.StatusEnum b : Course.StatusEnum.values()) {
                if (b.value.equals(value)) {
                    return b;
                }
            }
            throw new IllegalArgumentException("Unexpected value '" + value + "'");
        }
    }

}