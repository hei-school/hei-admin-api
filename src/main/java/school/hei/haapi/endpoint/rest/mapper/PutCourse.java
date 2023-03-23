package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@AllArgsConstructor
@Builder
@Data
@EqualsAndHashCode
@ToString
public class PutCourse {
    private String id;
    private String code;
    private Integer credits;
    private Integer total_hours;
    private String id_teacher;
}