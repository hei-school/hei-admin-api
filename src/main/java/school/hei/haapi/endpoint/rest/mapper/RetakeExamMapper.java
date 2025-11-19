package school.hei.haapi.endpoint.rest.mapper;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CancelRetakeExamRequest;
import school.hei.haapi.endpoint.rest.model.CrupdateRetakeExam;
import school.hei.haapi.endpoint.rest.model.RetakeExam;
import school.hei.haapi.endpoint.rest.model.RetakeExamStatus;
import school.hei.haapi.endpoint.rest.model.RetakeExamToCancel;
import school.hei.haapi.endpoint.rest.model.StudentRetakeExam;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.RetakeExamSession;
import school.hei.haapi.service.CourseService;
import school.hei.haapi.service.RetakeExamService;
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
  private final RetakeExamService retakeExamService;

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
        .status(
            school.hei.haapi.model.RetakeExamStatus.valueOf(crupdateRetakeExam.getStatus().name()))
        .build();
  }

  public school.hei.haapi.model.RetakeExam toDomainCrupdate(
      CancelRetakeExamRequest cancelRetakeExamRequest, RetakeExamStatus status) {
    var existingRetakeExam = retakeExamService.getById(cancelRetakeExamRequest.getRetakeExamId());
    existingRetakeExam.setStatus(school.hei.haapi.model.RetakeExamStatus.valueOf(status.name()));
    switch (status) {
      case TO_CANCEL -> existingRetakeExam.setCancelReason(cancelRetakeExamRequest.getReason());
      case REGISTERED -> existingRetakeExam.setRejectionReason(cancelRetakeExamRequest.getReason());
    }
    return existingRetakeExam;
  }

  public school.hei.haapi.model.RetakeExam toDomainCrupdate(
      RetakeExamToCancel retakeExamToCancel, RetakeExamStatus status) {
    var existingRetakeExam = retakeExamService.getById(retakeExamToCancel.getRetakeExamId());
    existingRetakeExam.setStatus(school.hei.haapi.model.RetakeExamStatus.valueOf(status.name()));
    return existingRetakeExam;
  }

  public List<school.hei.haapi.model.RetakeExam> toDomainCancelRetakeExamList(
      List<CancelRetakeExamRequest> cancelRetakeExamRequests,
      List<RetakeExamToCancel> retakeExamToCancels,
      RetakeExamStatus status) {
    if (cancelRetakeExamRequests != null) {
      return cancelRetakeExamRequests.stream()
          .map(cancelRetakeExamRequest -> this.toDomainCrupdate(cancelRetakeExamRequest, status))
          .toList();
    }
    return retakeExamToCancels.stream()
        .map(retakeExamToCancel -> this.toDomainCrupdate(retakeExamToCancel, status))
        .toList();
  }

  public RetakeExam toRest(school.hei.haapi.model.RetakeExam retakeExam) {
    var domainStatus = retakeExam.getStatus();
    var retakeExamRest =
        new RetakeExam()
            .id(retakeExam.getId())
            .course(courseMapper.toRest(retakeExam.getCourse()))
            .session(retakeExamSessionMapper.toRest(retakeExam.getSession()))
            .registrationDate(retakeExam.getRegistrationDate())
            .cancelReason(retakeExam.getCancelReason())
            .rejectionReason(retakeExam.getRejectionReason());
    if (domainStatus != null) {
      retakeExamRest.status(RetakeExamStatus.valueOf(domainStatus.name()));
    }
    return retakeExamRest;
  }

  public school.hei.haapi.model.RetakeExam toDomain(RetakeExam retakeExam) {
    return school.hei.haapi.model.RetakeExam.builder()
        .id(retakeExam.getId())
        .course(courseMapper.toDomain(retakeExam.getCourse()))
        .session(retakeExamSessionMapper.toDomain(retakeExam.getSession()))
        .status(school.hei.haapi.model.RetakeExamStatus.valueOf(retakeExam.getStatus().name()))
        .build();
  }

  public List<RetakeExam> toRestList(List<school.hei.haapi.model.RetakeExam> retakeExams) {
    return retakeExams.stream().map(this::toRest).toList();
  }

  public List<school.hei.haapi.model.RetakeExam> toDomainList(
      List<CrupdateRetakeExam> crupdateRetakeExams) {
    return crupdateRetakeExams.stream().map(this::toDomainCrupdate).toList();
  }

  public StudentRetakeExam toStudentRetakeRest(school.hei.haapi.model.RetakeExam retakeExam) {
    var domainStatus = retakeExam.getStatus();
    var studentRetakeExam =
        new StudentRetakeExam()
            .id(retakeExam.getId())
            .studentIdentifier(userMapper.toIdentifier(retakeExam.getStudent()))
            .course(courseMapper.toRest(retakeExam.getCourse()))
            .session(retakeExamSessionMapper.toRest(retakeExam.getSession()))
            .cancelReason(retakeExam.getCancelReason())
            .rejectionReason(retakeExam.getRejectionReason())
            .registrationDate(retakeExam.getRegistrationDate());
    if (domainStatus != null) {
      studentRetakeExam.status(RetakeExamStatus.valueOf(domainStatus.name()));
    }
    return studentRetakeExam;
  }

  public List<StudentRetakeExam> toStudentRetakeRestList(
      List<school.hei.haapi.model.RetakeExam> retakeExams) {
    return retakeExams.stream().map(this::toStudentRetakeRest).toList();
  }
}
