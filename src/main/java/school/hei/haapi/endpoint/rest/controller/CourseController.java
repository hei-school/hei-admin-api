package school.hei.haapi.endpoint.rest.controller;


import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.service.CourseService;
import school.hei.haapi.endpoint.rest.model.UpdateStudentCourse;
import school.hei.haapi.endpoint.rest.model.Course;
import java.util.List;
import java.io.*;

@RestController
@AllArgsConstructor
public class CourseController {
    private final CourseService courseService;
    private final CourseMapper courseMapper;
    @PutMapping("/students/{student_id}/courses")
    public Course updateCoursesByStatus(@RequestBody List<UpdateStudentCourse> course,@PathVariable String  student_id,String status){
       return courseMapper.toRestCourse(courseService.updateCourseStatus(student_id,course.get(0).getCourseId(),course.get(0).getStatus().toString()));
   }
}