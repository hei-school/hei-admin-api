package school.hei.haapi.unit;

import org.junit.jupiter.api.Test;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.User;
import static org.junit.jupiter.api.Assertions.assertNotEquals;


public class CourseTest {

    static Course course1(){
        return Course.builder()
                .id(TestUtils.COURSE1_ID)
                .code("PROG1")
                .name("Algo")
                .credits(100)
                .main_teacher(User.Role.TEACHER).build();
    }
    static Course course2(){
        return Course.builder()
                .id(TestUtils.COURSE2_ID)
                .code("WEB1")
                .name("Interface Web")
                .credits(100)
                .main_teacher(User.Role.TEACHER).build();
    }
    @Test
    void courses_with_unique_code_ok(){
        String course1Code = course1().getCode();
        String course2Code = course2().getCode();

        assertNotEquals(course1Code, course2Code);
    }
}
