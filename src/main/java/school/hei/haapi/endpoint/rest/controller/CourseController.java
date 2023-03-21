package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.service.CourseService;


@RestController
@AllArgsConstructor
public class CourseController {
    private final CourseMapper courseMapper;

    private final CourseService courseService;

    @GetMapping("/courses/{id}")
    public Course getCourseById(@PathVariable String id) {
        return courseMapper.toRest(courseService.getById(id));
    }

}
