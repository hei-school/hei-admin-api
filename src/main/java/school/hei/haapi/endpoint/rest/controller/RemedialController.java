package school.hei.haapi.endpoint.rest.controller;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import school.hei.haapi.endpoint.rest.model.CrupdateRemedial;
import school.hei.haapi.endpoint.rest.model.Exam;
import school.hei.haapi.endpoint.rest.model.Remedial;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.CourseAssignmentService;
import school.hei.haapi.endpoint.rest.mapper.RemedialMapper;
import school.hei.haapi.service.RemedialService;

@RestController
@AllArgsConstructor
public class RemedialController {
    private final RemedialService remedialService;
    private final RemedialMapper remedialMapper;

    @GetMapping("/remedials")
    public List<Remedial> getAllRemedials(
            @RequestParam(value = "page", defaultValue = "1") PageFromOne page,
            @RequestParam(value = "page_size", defaultValue = "15") BoundedPageSize pageSize,
            @RequestParam(value = "teacher_id", required = false) String teacherId,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "course_code", required = false) String courseCode,
            @RequestParam(value = "group_ref", required = false) String groupRef,
            @RequestParam(value = "remedial_date_from", required = false) Instant remedialDateFrom,
            @RequestParam(value = "remedial_date_to", required = false) Instant remedialDateTo) {
        return remedialMapper.toRestList(
                remedialService.getAllRemedials(
                        page,
                        pageSize,
                        teacherId,
                        title,
                        courseCode,
                        groupRef,
                        remedialDateFrom,
                        remedialDateTo));
    }

    @PutMapping("/remedials")
    public Remedial createOrUpdateRemedialsInfos(@RequestBody CrupdateRemedial remedialInfo) {
        return remedialMapper.toRest(
                 remedialService.updateOrSaveAll(List.of(remedialMapper.toDomain(remedialInfo))).getFirst());
    }

    @GetMapping("/remedials/{id}")
    public Remedial getRemedial(@PathVariable(name = "id") String id) {
        return remedialMapper.toRest(remedialService.getRemedialById(id));
    }
}
