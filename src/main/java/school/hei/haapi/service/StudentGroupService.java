package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.StudentGroup;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.validator.StudentGroupValidator;
import school.hei.haapi.repository.GroupRepository;
import school.hei.haapi.repository.StudentGroupRepository;
import school.hei.haapi.repository.UserRepository;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class StudentGroupService {
    private final StudentGroupValidator StudentGroupValidator;
    private final StudentGroupRepository StudentGroupRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;


    public List<StudentGroup> getAll(
    ){
        return StudentGroupRepository.findAll();
    }



    @Transactional
    public StudentGroup accept(StudentGroup StudentGroup){
        StudentGroupValidator.accept(StudentGroup);
        Group group = groupRepository
                .findById(StudentGroup.getGroup().getId())
                .orElseThrow(() ->
                        new BadRequestException("Group with id "+StudentGroup
                                .getGroup()
                                .getId()+"doesn't exist"));
        User student = userRepository
                .findById(StudentGroup.getStudent().getId())
                .orElseThrow(() ->
                        new BadRequestException("User with id "+StudentGroup
                                .getStudent()
                                .getId()+"doesn't exist"));
        StudentGroup.setGroup(group);
        StudentGroup.setStudent(student);
        return StudentGroup;
    }

    @Transactional
    public List<StudentGroup> saveAll(List<StudentGroup> StudentGroups){
        List<StudentGroup> saved = new ArrayList<>();
        StudentGroups
                .forEach(StudentGroup ->
                        saved.add(accept(StudentGroup)));
        return StudentGroupRepository.saveAll(saved);
    }

    public List<User> getStudentsByGroupsId(String group_id) {
        List<User> users = new ArrayList<>();
        Group group = groupRepository.findById(group_id)
                .orElseThrow(() ->
                        new BadRequestException("Group with id "+group_id+" doesn't exist"));
        List<StudentGroup> studentGroups = StudentGroupRepository.findByGroupId(group_id);
        for (StudentGroup u:studentGroups) {
            users.add(u.getStudent());
        }
        return users;
    }
}
