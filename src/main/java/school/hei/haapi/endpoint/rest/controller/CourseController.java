package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.model.Course;
import school.hei.haapi.service.CourseService;

import java.util.List;

@RestController
@AllArgsConstructor
public class CourseController {
    private final CourseService courseService;

    @GetMapping("/courses")
    public List<Course> getAllCourseByTeacherFirstName(
            @RequestParam(name = "teacher_first_name", required = false) String teacherFirstName,
            @RequestParam(name = "teacher_last_name",required = false) String teacherLastName,
            @RequestParam(name = "code", required = false)String code,
            @RequestParam(name ="name", required = false)String name
    ) {
      if(teacherFirstName != null){
          return courseService.getAllCoursesByTeacherFirstName(teacherFirstName);
      }
      else if(teacherLastName != null){
          return courseService.getAllCourseByTeacherLastName(teacherLastName);
      }
      else if(code != null){
          return courseService.getAllCourseByCode(code);
      }
      else if(name != null){
          return courseService.getAllCourseByName(name);
      }
return courseService.getAllCourse();
    }
}
