package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.mapper.FeeMapper;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.CourseService;
import school.hei.haapi.service.FeeService;

import java.util.List;

import static java.util.stream.Collectors.toUnmodifiableList;

@RestController
@AllArgsConstructor
public class CourseController {
    private final CourseService courseService;
    private final CourseMapper courseMapper;

    @GetMapping("/students/{studentId}/courses")
    public List<Course> getFeesByStudentId(
            @PathVariable String studentId,
            @RequestParam PageFromOne page,
            @RequestParam("page_size") BoundedPageSize pageSize,
            @RequestParam(required = false) CourseStatus status) {
        return courseService.getCoursesByStudentId(studentId, page, pageSize, status).stream()
                .map(courseMapper::toRestCourse)
                .collect(toUnmodifiableList());
    }
}
