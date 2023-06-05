package school.hei.haapi.endpoint.rest.mapper;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.StudentGrade;
import school.hei.haapi.model.Grade;
import school.hei.haapi.model.User;
import school.hei.haapi.service.UserService;
import java.time.ZoneId;

@Component
@AllArgsConstructor
public class GradeMapper {
  private final UserService userService;

  public Grade toDomain(StudentGrade studentGrade) {
    school.hei.haapi.endpoint.rest.model.Grade gradeRest = studentGrade.getGrade();
    User user = userService.getById(studentGrade.getId());

    return Grade.builder().user(user).examId(studentGrade.getRef())
        .score(gradeRest.getScore().intValue())
        .creationDateTime(LocalDateTime.ofInstant(gradeRest.getCreatedAt(), ZoneId.systemDefault()))
        .build();
  }

  public school.hei.haapi.endpoint.rest.model.StudentGrade toRest(Grade grade) {
    StudentGrade studentGrade = new StudentGrade();
    studentGrade.setId(grade.getUser().getId());
    studentGrade.setRef(grade.getUser().getRef());
    studentGrade.setFirstName(grade.getUser().getFirstName());
    studentGrade.setLastName(grade.getUser().getLastName());
    studentGrade.setEmail(grade.getUser().getEmail());
    school.hei.haapi.endpoint.rest.model.Grade restGrade = toRestGrade(grade);
    studentGrade.setGrade(restGrade);

    return studentGrade;
  }

  public school.hei.haapi.endpoint.rest.model.Grade toRestGrade(Grade grade) {
    return new school.hei.haapi.endpoint.rest.model.Grade().score((double) grade.getScore())
        .createdAt(grade.getCreationDateTime().atZone(ZoneId.systemDefault()).toInstant());
  }
}
