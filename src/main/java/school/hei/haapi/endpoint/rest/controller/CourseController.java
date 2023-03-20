package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.model.Group;
import school.hei.haapi.model.Course;
import school.hei.haapi.service.CourseService;

import java.util.List;

import static java.util.stream.Collectors.toUnmodifiableList;

@RestController
@AllArgsConstructor
public class CourseController {
    private final CourseService courseService;
    private final CourseMapper courseMapper;

    @PutMapping(value = "/courses")
    public List<Course> createOrUpdateCourses(@RequestBody List<Course> toWrite) {
        var saved = courseService.saveAll(toWrite.stream()
                .map(courseMapper::toDomain)
                .collect(toUnmodifiableList()));
        return saved.stream()
                .map(courseMapper::toRest)
                .collect(toUnmodifiableList());
    }
}
