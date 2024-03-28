package school.hei.haapi.endpoint.rest.mapper;

import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CrupdateWorkStudyStudent;
import school.hei.haapi.endpoint.rest.model.Student;
import school.hei.haapi.endpoint.rest.model.WorkStudyInfo;
import school.hei.haapi.endpoint.rest.model.WorkStudyStudent;
import school.hei.haapi.model.User;
import school.hei.haapi.model.WorkInfo;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.service.UserService;

@Component
@AllArgsConstructor
public class WorkInfoMapper {
  private final UserService userService;
  private final UserMapper userMapper;

  public List<WorkInfo> toDomain(String studentId, List<CrupdateWorkStudyStudent> toSave) {
    acceptClientPayloadRequest(studentId, toSave);

    User student = userService.findById(studentId);

    return toSave.stream()
        .map(workStudyStudent -> toDomain(workStudyStudent, student))
        .collect(Collectors.toList());
  }

  public WorkStudyStudent toRest(String studentId, List<WorkInfo> domain) {
    Student student = userMapper.toRestStudent(userService.findById(studentId));
    List<WorkStudyInfo> restWorkInfo =
        domain.stream().map(this::toRest).collect(Collectors.toList());

    return new WorkStudyStudent()
        .id(student.getId())
        .nic(student.getNic())
        .ref(student.getRef())
        .email(student.getEmail())
        .firstName(student.getFirstName())
        .lastName(student.getLastName())
        .workStudyInfo(restWorkInfo);
  }

  public WorkInfo toDomain(CrupdateWorkStudyStudent toSave, User student) {
    return WorkInfo.builder()
        .id(toSave.getId())
        .business(toSave.getBusiness())
        .company(toSave.getCompany())
        .commitmentBeginDate(toSave.getCommitmentBeginDate())
        .commitmentEndDate(toSave.getCommitmentEndDate())
        .student(student)
        .build();
  }

  public WorkStudyInfo toRest(WorkInfo domainWorkInfo) {
    return new WorkStudyInfo()
        .id(domainWorkInfo.getId())
        .company(domainWorkInfo.getCompany())
        .business(domainWorkInfo.getBusiness())
        .commitmentBeginDate(domainWorkInfo.getCommitmentBeginDate())
        .commitmentEndDate(domainWorkInfo.getCommitmentEndDate());
  }

  private void acceptClientPayloadRequest(String studentId, List<CrupdateWorkStudyStudent> toSave) {
    toSave.forEach(
        toCreate -> {
          if (!toCreate.getStudentId().equals(studentId)) {
            throw new BadRequestException(
                "Student id requests didn't match with student id in payload");
          }
        });
  }
}
