package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.CourseService;

import java.util.List;

import static java.util.stream.Collectors.toUnmodifiableList;

@RestController
@AllArgsConstructor
public class CourseController {
    private CourseMapper courseMapper;
    private CourseService service;

    @GetMapping("/courses")
    public List<Course> getAllCourses(
            @RequestParam PageFromOne page, @RequestParam("page_size") BoundedPageSize pageSize,
            @RequestParam(value = "ref", required = false, defaultValue = "") String ref,
            @RequestParam(value = "name", required = false, defaultValue = "") String name,
            @RequestParam(value = "total_hours", required = false, defaultValue = "") Integer total_hours,
            @RequestParam(value = "credits", required = false, defaultValue = "") Integer credits){
                return service.getAllCourses().stream()
                        .map(courseMapper::toRestCourse)
                        .collect(toUnmodifiableList());
    }

    @PutMapping("/courses")
    public List<Course> saveAllCourses(@RequestBody List<Course> toCreate) {
        return service
                .saveAllCourses(toCreate
                        .stream()
                        .map(courseMapper::toDomain)
                        .collect(toUnmodifiableList()))
                .stream()
                .map(courseMapper::toRestCourse)
                .collect(toUnmodifiableList());
    }

}