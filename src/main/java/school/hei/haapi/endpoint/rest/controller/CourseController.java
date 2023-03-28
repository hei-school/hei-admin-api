package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.service.CourseService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
public class CourseController {
    private final CourseService courseService;
    private final CourseMapper courseMapper;
    @GetMapping("/courses")
    public List<Course> getCourses(@RequestParam(name = "code",required = false, defaultValue = "ASC") String code,
                                   @RequestParam(name = "name",required = false) String name,
                                   @RequestParam(name = "credits",required = false, defaultValue = "ASC") Integer credits,
                                   @RequestParam(name = "teacher_first_name",required = false) String teacherFirstName,
                                   @RequestParam(name = "teacher_last_name",required = false) String teacherLastName) {
        List<school.hei.haapi.model.Course> filter = courseService.getCourses(code, name, credits, teacherFirstName, teacherLastName);
        return filter.stream()
                .map(courseMapper::toRest)
                .collect(Collectors.toList());
    }
}
