package school.hei.haapi.unit;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.model.Teacher;
import school.hei.haapi.model.Courses;
import school.hei.haapi.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

@Slf4j
public class CourseMapperTest {
    CourseMapper courseMapper = mock(CourseMapper.class);
    @Test
    void course_to_domain_ok(){

        Teacher teacher1 = new Teacher();
        User user1 = User.builder().build();

        Course course = new Course();
        course.setId("1");
        course.setName("Algorithmique");
        course.setCode("PROG1");
        course.setCredits(6);
        course.setTotalHours(30);
        course.setMainTeacher(teacher1);
        Courses expected = Courses.builder()
                .id(course.getId())
                .credits(course.getCredits())
                .main_teacher(user1)
                .total_hours(course.getTotalHours())
                .name(course.getName())
                .code(course.getCode())
                .build();
        Courses actual = courseMapper.toDomain(course);
        assertEquals(expected, actual);
    }
}
