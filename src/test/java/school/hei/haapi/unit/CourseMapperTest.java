package school.hei.haapi.unit;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.mapper.UserMapper;
import school.hei.haapi.endpoint.rest.model.Courses;
import school.hei.haapi.endpoint.rest.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.List;

@Slf4j
public class CourseMapperTest {
    CourseMapper courseMapper = mock(CourseMapper.class);
    @Test
    void course_to_domain_ok(){
        Courses courses = Courses.builder()
                .id("1")
                .name("Algorithme")
                .code("PROG1")
                .total_hours("0")
                .main_teacher(User.builder()
                        .role(User.Role.TEACHER)
                        .build())
                .build();
        Courses exepted = Courses.builder()
                .id(courses.getId())
                .credits(courses.getCredits())
                .main_teacher(courses.getMain_teacher())
                .total_hours(courses.getTotal_hours())
                .name(courses.getName())
                .code(courses.getCode())
                .build();
        when(courseMapper.toDomain(courses)).thenReturn(courses);
        Courses actual = courseMapper.toDomain(courses);
        assertEquals(exepted, actual);
    }
}
