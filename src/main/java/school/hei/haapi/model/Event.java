package school.hei.haapi.model;


import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import school.hei.haapi.endpoint.rest.model.EventType;

import java.time.Instant;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static org.hibernate.type.SqlTypes.NAMED_ENUM;

@Entity
@Table(name = "\"event\"")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private String id;

    @JdbcTypeCode(NAMED_ENUM)
    @Enumerated(STRING)
    private EventType type;

    private String description;

    private Instant begin;

    private Instant end;

    @ManyToOne
    @JoinColumn(name = "planner_id", referencedColumnName = "id")
    private User planner;

    @ManyToOne
    @JoinColumn(name = "course_id", referencedColumnName = "id")
    private Course course;

}
