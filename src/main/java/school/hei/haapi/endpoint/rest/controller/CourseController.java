package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.CourseService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
public class CourseController {
    private final CourseMapper mapper;

    private final CourseService service;

    @GetMapping("/courses")
    public List<Course> getCourses(@RequestParam(value = "page", defaultValue = "1") PageFromOne page,
                                   @RequestParam(value = "page_size", defaultValue = "15")
                                   BoundedPageSize pageSize
    ) {
        return service.getCourses(page, pageSize)
                .stream()
                .map(mapper::toRest)
                .collect(Collectors.toUnmodifiableList());
    }
}
