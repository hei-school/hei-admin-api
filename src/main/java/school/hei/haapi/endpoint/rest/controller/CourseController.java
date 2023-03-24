package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Courses;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.CourseService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
public class CourseController {
    private final CourseService courseService;
    private final CourseMapper courseMapper;
    @GetMapping(value = "/courses")
    public List<Course> getCourses(
            @RequestParam PageFromOne page, @RequestParam("page_size") BoundedPageSize pageSize,
            @RequestParam(value = "code", required = false, defaultValue = "") String code,
            @RequestParam(value = "codeOrder", required = false, defaultValue = "DESC") String codeOrder,
            @RequestParam(value = "name", required = false, defaultValue = "") String name,
            @RequestParam(value = "credits", required = false, defaultValue = "") Integer credits,
            @RequestParam(value = "creditsOrder", required = false, defaultValue = "DESC") String creditsOrder,
            @RequestParam(value = "teacher_first_name", required = false, defaultValue = "") String teacher_first_name,
            @RequestParam(value = "teacher_last_name", required = false, defaultValue = "") String teacher_last_name){
        return  courseService.getByCriteria(code, codeOrder, name, credits, creditsOrder, teacher_last_name, teacher_first_name,page, pageSize)
                .stream()
                .map(courseMapper::toRest)
                .collect(Collectors.toUnmodifiableList());
    }
}

