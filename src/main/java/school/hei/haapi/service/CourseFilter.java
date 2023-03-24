package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourseFilter {
    private String code;
    private String name;
    private Integer credits;
    private String teacherFirstName;
    private String teacherLastName;
}
