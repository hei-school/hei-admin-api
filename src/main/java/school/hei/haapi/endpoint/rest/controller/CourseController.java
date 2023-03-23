package school.hei.haapi.endpoint.rest.controller;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.CourseService;

import static java.util.stream.Collectors.toUnmodifiableList;

@RestController
@AllArgsConstructor
public class CourseController {
    private final CourseService service;
    private final CourseMapper courseMapper;

    @GetMapping(value = "/courses")
    public List<Course> getCourses(
            @RequestParam(name = "page") PageFromOne page,
            @RequestParam(name = "page_size") BoundedPageSize pageSize
    ) {
        return service.getCourses(page, pageSize).stream()
                .map(courseMapper::toRest)
                .collect(toUnmodifiableList());
    }
}
