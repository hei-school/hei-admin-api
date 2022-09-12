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
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import school.hei.haapi.repository.types.PostgresEnumType;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "\"event\"")
@TypeDef(name = "pgsql_enum", typeClass = PostgresEnumType.class)
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Event implements Serializable {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private String id;

    @Type(type = "pgsql_enum")
    @Enumerated(EnumType.STRING)
    private EventTypeEnum eventType;

    @NotBlank(message = "Description is mandatory")
    private String description;

    @ManyToOne
    private User responsible;

    private Instant start;
    private Instant end;

    @Type(type = "pgsql_enum")
    @Enumerated(EnumType.STRING)
    private StatusEnum status;

    @ManyToOne
    private Place place;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        Event event = (Event) o;
        return id != null && Objects.equals(id, event.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public enum EventTypeEnum {
            COURSES,
            EXAMS,
            CONFERENCES
    }

    public enum StatusEnum {
        ACTIVE,
        CANCEL
    }
}
