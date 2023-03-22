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
    public List<Course> getAllCourseByTeacherFirstName (@RequestParam(name = "teacher_first_name") String teacherFirstName) {
        return courseService.getAllCoursesByTeacherFirstName(teacherFirstName);
    }
}
