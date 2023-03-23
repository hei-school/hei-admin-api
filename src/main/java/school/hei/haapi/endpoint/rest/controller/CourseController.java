package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.CourseService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
public class CourseController {
    private CourseService service;
    @GetMapping("/courses")
    public List<Course> getCourse(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) int credits,
            @RequestParam(required = false) String teacherFirstName,
            @RequestParam(required = false) String teacherLastName,
            @RequestParam(required = false) String codeOrder,
            @RequestParam(required = false) String creditsOrder,
            @RequestParam(defaultValue = "1") PageFromOne page,
            @RequestParam(defaultValue = "15") BoundedPageSize pageSize
            ) {
        return service.GetAllAndFiltreReturnedList(
                code,
                name,
                credits,
                teacherFirstName,
                teacherLastName,
                codeOrder,
                creditsOrder,
                page,
                pageSize
        );
    }
}
