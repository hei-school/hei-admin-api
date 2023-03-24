package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.model.CourseStatus;
import school.hei.haapi.endpoint.rest.model.CrupdateCourse;
import school.hei.haapi.endpoint.rest.model.UpdateStudentCourse;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.CourseService;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toUnmodifiableList;

@RestController
@AllArgsConstructor
public class CourseController {
    private final CourseService service;
    private final CourseMapper mapper;

    @GetMapping(value = "/courses")
    public List<Course> getCourses(
            @RequestParam(name = "page", required = false, defaultValue = "1") PageFromOne page,
            @RequestParam(name = "page_size", required = false, defaultValue = "15") BoundedPageSize pageSize
    ){
        return service.getCourses(page, pageSize).stream()
                .map(mapper::toRest)
                .collect(Collectors.toUnmodifiableList());
    }
}
