package school.hei.haapi.endpoint.rest.controller;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
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
    private final CourseMapper courseMapper;
    private final CourseService courseService;

    @GetMapping("/courses")
    public List<Course> getCourses(@RequestParam(value = "page", required = false) PageFromOne page,
                                   @RequestParam(value = "page_size", required = false)
                                   BoundedPageSize pageSize,
                                   @RequestParam(value = "code", required = false, defaultValue = "")
                                   String code,
                                   @RequestParam(value = "name", required = false, defaultValue = "")
                                   String name,
                                   @RequestParam(value = "credits", required = false) Integer credits,
                                   @RequestParam(value = "teacher_first_name", required = false,
                                           defaultValue = "")
                                   String teacherFirstName,
                                   @RequestParam(value = "teacher_last_name", required = false, defaultValue = "")String teacherLastName,
                                   @RequestParam(value = "codeOrder", required = false,
                                           defaultValue = "ASC")
                                       String codeOrder,
                                   @RequestParam(value = "creditsOrder", required = false,
                                           defaultValue = "ASC")
                                       String creditsOrder){
        return courseService.findByCriteria(page, pageSize, code, name, credits, teacherFirstName, teacherLastName)
                .stream()
                .map(courseMapper::toRest)
                .collect(toUnmodifiableList());
    }
}