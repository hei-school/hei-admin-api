package school.hei.haapi.endpoint.rest.controller;

import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import school.hei.haapi.endpoint.rest.mapper.CoursesMapper;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Courses;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.CoursesService;

@RestController
@AllArgsConstructor
public class CoursesController {
    private final CoursesService service;
    private final CoursesMapper mapper;

    @GetMapping(value = "/courses")
    public List<Courses> getCourses(
            @RequestParam PageFromOne page,
            @RequestParam("page_size") BoundedPageSize pageSize,
            @RequestParam(required = false, defaultValue = "") String ref
    ){
        return service.getByCode(ref, page, pageSize).stream()
                .map(mapper::toRest)
                .collect(Collectors.toUnmodifiableList());
    }
}