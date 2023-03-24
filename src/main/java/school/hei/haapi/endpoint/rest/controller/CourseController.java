package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.model.Order;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.CourseService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
public class CourseController {
    private final CourseService service;
    private final CourseMapper mapper;

    @GetMapping(value = "/courses")
    public List<school.hei.haapi.endpoint.rest.model.Course> getCourses(
                                   @RequestParam(name = "page") PageFromOne page, @RequestParam("page_size") BoundedPageSize pageSize,
                                   @RequestParam(name = "code",required = false) String code,
                                   @RequestParam(name = "name",required = false) String name,
                                   @RequestParam(name = "credits",required = false, defaultValue = "ASC") Integer credits,
                                   @RequestParam(name = "teacher_first_name",required = false) String teacherFirstName,
                                   @RequestParam(name = "teacher_last_name",required = false) String teacherLastName,
                                   @RequestParam(name = "code_order", required = false, defaultValue = "ASC") Order codeOrder,
                                   @RequestParam(name = "credits_order", required = false, defaultValue = "ASC") Order creditsOrder) {
        List<school.hei.haapi.model.Course> filtered = service.getCourses(page, pageSize, code, name, credits, teacherFirstName, teacherLastName, codeOrder, creditsOrder);
        return filtered.stream().map(mapper::toRest).collect(Collectors.toUnmodifiableList());}
}
