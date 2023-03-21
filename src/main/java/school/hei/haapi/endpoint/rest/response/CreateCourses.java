package school.hei.haapi.endpoint.rest.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateCourses {
    private String id;
    private String code;
    private String name;
    private int credits;
    private int total_hours;
    private String main_teacher_id;
}
