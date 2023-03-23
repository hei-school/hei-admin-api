package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.CourseService;

import java.util.List;

@RestController
@AllArgsConstructor
public class CourseController {
    private final CourseService courseService;

    @GetMapping("/courses")
    public List<Course> getAllCourseByTeacherFirstName(
            @RequestParam(name = "page",required = false) PageFromOne page,
            @RequestParam(name = "page_size",required = false) BoundedPageSize pageSize,
            @RequestParam(name = "teacher_first_name", required = false) String teacherFirstName,
            @RequestParam(name = "teacher_last_name",required = false) String teacherLastName,
            @RequestParam(name = "code", required = false)String code,
            @RequestParam(name ="name", required = false)String name,
            @RequestParam(name = "credits",required = false)Integer credits
    ) {
      if(teacherFirstName != null){
          return courseService.getAllCoursesByTeacherFirstName(page,pageSize,teacherFirstName);
      }
      else if(teacherLastName != null){
          return courseService.getAllCourseByTeacherLastName(page,pageSize,teacherLastName);
      }
      else if(code != null){
          return courseService.getAllCourseByCode(page,pageSize,code);
      }
      else if(name != null){
          return courseService.getAllCourseByName(page,pageSize,name);
      }
      else if(credits != null){
          return courseService.getAllCourseByCredits(page,pageSize,credits);
      }
return courseService.getAllCourse();
    }
}
