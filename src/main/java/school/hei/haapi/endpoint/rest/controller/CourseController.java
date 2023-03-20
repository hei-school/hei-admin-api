package school.hei.haapi.endpoint.rest.controller;

import io.swagger.model.Course;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import school.hei.haapi.service.CourseService;
import school.hei.haapi.endpoint.rest.model.UpdateStudentCourse;
import java.util.List;

@RestController
@AllArgsConstructor
public class CourseController {
    private final CourseService courseService;
    @PutMapping("/students/{student_id}/courses")
    public Course updateCoursesByStatus(@RequestBody List<UpdateStudentCourse> course,@PathVariable String  student_id,String status){
       return courseService.updateCourseStatus(student_id,course.get(0).courseId,course.get(0).status);
   }
}