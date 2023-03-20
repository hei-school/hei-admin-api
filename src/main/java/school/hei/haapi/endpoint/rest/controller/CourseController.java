package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.model.Course;
import school.hei.haapi.service.CourseService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
public class CourseController {
    private CourseService service;
    private CourseMapper mapper;

    @PostMapping("/courses")
    public List<Course> updateCourse(@PathVariable int id, @RequestBody List<Course> coursesourse){
        return service.updateCourse(id, coursesourse).stream()
                .map(mapper::ToDomain)
                .collect(Collectors.toUnmodifiableList());
    }
}
