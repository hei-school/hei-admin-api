package school.hei.haapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import school.hei.haapi.endpoint.rest.model.CourseStatus;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CourseFollowedRest {

    private String course_id;

    @Type(type = "psql_enum")
    @Enumerated(EnumType.STRING)
    private CourseStatus status;

}
