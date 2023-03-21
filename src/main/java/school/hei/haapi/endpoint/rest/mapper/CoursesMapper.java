package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.model.CrupdateCourse;
import school.hei.haapi.endpoint.rest.model.Teacher;
import school.hei.haapi.model.Courses;
import school.hei.haapi.model.User;
import school.hei.haapi.service.UserService;

@Component
@AllArgsConstructor
public class CoursesMapper {
    public school.hei.haapi.endpoint.rest.model.Courses toRestCourses(Courses courses){
        var restCourses = new school.hei.haapi.endpoint.rest.model.Course();
        restCourses.setId(course.getId());
        restCourses.setRef(course.getRef());
        restCourses.setName(course.getName());
        restCourses.setCredits(course.getCredits());
        restCourses.setTotalHours(course.getTotalHours());
        restCourses.setMainTeachers(course.getMainTeacher());
        return restCourses;
    }

    public Courses toDomain(school.hei.haapi.endpoint.rest.model.Courses restCourses){
        return Courses.builder()
                .id(restCourses.getId())
                .ref(restCourses.getCode())
                .name(restCourses.getName())
                .credits(restCourses.getCredits())
                .totalHours(restCourses.getTotalHours())
                .mainTeacher(restCourses.getMainTeacher())
                .build();
    }
}


