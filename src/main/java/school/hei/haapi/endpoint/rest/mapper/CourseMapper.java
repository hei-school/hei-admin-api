package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.Course;

@Component
@AllArgsConstructor
public class CourseMapper {
    public Course toDomain (Course course){
        return Course.builder()
                .id(course.getId())
                .name(course.getName())
                .credits(course.getCredits())
                .ref(course.getRef())
                .total_hours(course.getTotal_hours())
                .build();
    }
//    public List<school.hei.haapi.model.Course> toDomainC (String studentId, List<Course> toCreate){
//        Course course = service.getById(studentId);
//        if (status == null) {
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
//
//    public Course toRestC(Course course) {
//        return new school.hei.haapi.model.Course()
//                .id(course.getId())
//                .status(course.getStatus())
//                .studentId(course.getStudent().getId())
//                .to(course.getTotal_hours())
//                .credits(course.getCredits())
//                .ref(course.getRef())
//                .name(course.getName());
//    }
}
