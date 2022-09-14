package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
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

    @GetMapping(value = "/courses/{id}")
    public Course getCourseById(@PathVariable String id) {
        return courseMapper.toRest(courseService.getById(id));
    }

    @GetMapping(value = "/courses")
    public List<Course> getCourses() {
        return courseService.getAll().stream()
                .map(courseMapper::toRest)
                .collect(Collectors.toUnmodifiableList());
    }
    @PutMapping("/courses")
    public school.hei.haapi.model.Course createOrUpdate(@RequestBody school.hei.haapi.model.Course toWrite) {
        return courseService.save(toWrite);
    }
}
