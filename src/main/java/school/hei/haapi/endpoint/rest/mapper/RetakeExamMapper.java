package school.hei.haapi.endpoint.rest.mapper;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CrupdateRetakeExam;
import school.hei.haapi.endpoint.rest.model.RetakeExam;
import school.hei.haapi.endpoint.rest.model.StudentRetakeExam;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.RetakeExamSession;
import school.hei.haapi.service.CourseService;
import school.hei.haapi.service.RetakeExamSessionService;
import school.hei.haapi.service.UserService;

@Component
@AllArgsConstructor
public class RetakeExamMapper {
  private final CourseMapper courseMapper;
  private final RetakeExamSessionMapper retakeExamSessionMapper;
  private final UserService userService;
  private final CourseService courseService;
  private final RetakeExamSessionService retakeExamSessionService;
  private final UserMapper userMapper;

  public school.hei.haapi.model.RetakeExam toDomainCrupdate(CrupdateRetakeExam crupdateRetakeExam) {
    var studentUser = userService.getById(crupdateRetakeExam.getStudentId());
    RetakeExamSession retakeExamSession =
        retakeExamSessionService.getById(crupdateRetakeExam.getSessionId());
    Course course = courseService.getById(crupdateRetakeExam.getCourseId());
    return school.hei.haapi.model.RetakeExam.builder()
        .id(crupdateRetakeExam.getId())
        .student(studentUser)
        .session(retakeExamSession)
        .course(course)
        .build();
  }

  public RetakeExam toRest(school.hei.haapi.model.RetakeExam retakeExam) {
    return new RetakeExam()
        .id(retakeExam.getId())
        .course(courseMapper.toRest(retakeExam.getCourse()))
        .session(retakeExamSessionMapper.toRest(retakeExam.getSession()))
        .registrationDate(retakeExam.getRegistrationDate());
  }

  public school.hei.haapi.model.RetakeExam toDomain(RetakeExam retakeExam) {
    return school.hei.haapi.model.RetakeExam.builder()
        .id(retakeExam.getId())
        .course(courseMapper.toDomain(retakeExam.getCourse()))
        .session(retakeExamSessionMapper.toDomain(retakeExam.getSession()))
        .build();
  }

  public List<RetakeExam> toRestList(List<school.hei.haapi.model.RetakeExam> retakeExams) {
    return retakeExams.stream().map(this::toRest).toList();
  }

  public List<school.hei.haapi.model.RetakeExam> toDoMainList(
      List<CrupdateRetakeExam> crupdateRetakeExams) {
    return crupdateRetakeExams.stream().map(this::toDomainCrupdate).toList();
  }

  public StudentRetakeExam toStudentRetakeRest(school.hei.haapi.model.RetakeExam retakeExam) {
    return new StudentRetakeExam()
        .studentIdentifier(userMapper.toIdentifier(retakeExam.getStudent()))
        .course(courseMapper.toRest(retakeExam.getCourse()))
        .session(retakeExamSessionMapper.toRest(retakeExam.getSession()))
        .registrationDate(retakeExam.getRegistrationDate());
  }

  public List<StudentRetakeExam> toStudentRetakeRestList(
      List<school.hei.haapi.model.RetakeExam> retakeExams) {
    return retakeExams.stream().map(this::toStudentRetakeRest).toList();
  }
}
