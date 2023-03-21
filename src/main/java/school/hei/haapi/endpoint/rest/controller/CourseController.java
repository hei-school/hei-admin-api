package school.hei.haapi.endpoint.rest.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.CourseService;

import java.util.List;
import java.util.stream.Collectors;

public class CourseController {
    private CourseService courseService;
    private CourseMapper courseMapper;


    @GetMapping("/courses")
    public List<Course> getCourses(
            @RequestParam(name = "page", required = false) PageFromOne page,
            @RequestParam(name = "page_size", required = false) BoundedPageSize pageSize
    ){
        return courseService.getCourses(page, pageSize).stream()
                .map(courseMapper::toRestCourse)
                .collect(Collectors.toList());
    }
}

