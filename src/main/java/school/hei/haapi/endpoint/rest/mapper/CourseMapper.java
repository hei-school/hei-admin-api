package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.Course;
import school.hei.haapi.service.CourseService;

@Component
@AllArgsConstructor
public class CourseMapper {
    private final CourseService service;
    public Course toDomain (Course course){
        return Course.builder()
                .id(course.getId())
                .name(course.getName())
                .credits(course.getCredits())
                .ref(course.getRef())
                .total_hours(course.getTotal_hours())
                .build();
    }
//    public Course toDomainC (String studentId, Course toCreate){
//        Course course = service.getById(studentId);
//        if (course == null) {
//            throw new NotFoundException("Course.id=" + studentId + " is not found");
//        }
//        String id = "";
//        return toCreate
//                .stream()
//                .map(Course -> toDomainC(id, course))
//                .collect(toUnmodifiableList());
//    }
    public Course toRest (Course course){
        Course nCourse = new Course();
        nCourse.setId(course.getId());
        nCourse.setRef(course.getRef());
        nCourse.setName(course.getName());
        nCourse.setCredits(course.getCredits());
        nCourse.setTotal_hours(course.getTotal_hours());

        return nCourse;
    }
}
