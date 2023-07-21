package school.hei.haapi.model;

import lombok.*;
import org.hibernate.annotations.Type;
import school.hei.haapi.endpoint.rest.model.CourseStatus;

import javax.persistence.*;

import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "\"transcript\"")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Transcript {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private String id;

    @ManyToOne
    @JoinColumn(name = "student_id", referencedColumnName = "id")
    private User student_id;
    private String semester;
    private  String academicYear;
    private String isDefinitive;
    private Date creation_datetime;
}
