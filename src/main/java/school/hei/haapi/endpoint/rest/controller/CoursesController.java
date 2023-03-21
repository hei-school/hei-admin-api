package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.CoursesMapper;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.service.CoursesService;

import java.util.List;

import static java.util.stream.Collectors.toUnmodifiableList;

@RestController
@AllArgsConstructor
public class CoursesController {
    private final CoursesService coursesService;
    private final CoursesMapper courseMapper;

    @GetMapping(value = "/courses")
    public List<Course> getCourses() {
        return coursesService.getAll().stream()
                .map(courseMapper::toRest)
                .collect(toUnmodifiableList());
    }
}
