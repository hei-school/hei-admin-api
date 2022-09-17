package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.UserMapper;
import school.hei.haapi.endpoint.rest.model.Student;
import school.hei.haapi.model.StudentGroup;
import school.hei.haapi.service.StudentGroupService;

import java.util.List;

import static java.util.stream.Collectors.toUnmodifiableList;

@RestController
@AllArgsConstructor
@CrossOrigin
public class StudentGroupController {
    private StudentGroupService studentGroupService;
    //private StudentGroupMapper studentGroupMapper;
    private final UserMapper userMapper;

    @GetMapping("/student_groups")
    public List<StudentGroup> getStudentGroups(
    ){
        return studentGroupService.getAll();
    }

    @GetMapping("/groups/{group_id}/students")
    public List<Student> getEventParticipantsByEventsId(
            @PathVariable String group_id
    ){
        return studentGroupService.getStudentsByGroupsId(group_id).stream()
                .map(userMapper::toRestStudent)
                .collect(toUnmodifiableList());
    }

    @PostMapping("/student_groups")
    public List<StudentGroup> createOrUpdateStudentGroups(
            @RequestBody List<StudentGroup> toCreate){
        return studentGroupService
                .saveAll(toCreate);
    }
}
