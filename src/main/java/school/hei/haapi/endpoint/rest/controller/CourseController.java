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
    private CourseService courseService;
    private CourseMapper courseMapper;

    @GetMapping("/courses")
    public List<Course> getAllCourses(
            @RequestParam PageFromOne page, @RequestParam("page_size") BoundedPageSize pageSize){
        return courseService.getAll();
    }
    @GetMapping("/courses/{courseId}")
    public Course getCourseById(
            @PathVariable String courseId){
        return courseService.getById(courseId);
    }
    @GetMapping("/course/{courseName}")
    public List<Course> getCourseByName(
            @PathVariable String courseName){
        return courseService.getAllByName(courseName);
    }

    @PutMapping("/course")
    public List<Course> createOrUpdateCourse(@RequestBody List<Course> toWrite) {
        var saved = courseService.saveAll(toWrite.stream()
                .map(courseMapper::toDomain)
                .collect(toUnmodifiableList()));
        return saved.stream()
                .map(courseMapper::toRest)
                .collect(toUnmodifiableList());
    }
}
