package school.hei.haapi.endpoint.rest.mapper;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CrupdateRetakeExam;
import school.hei.haapi.endpoint.rest.model.RetakeExam;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.RetakeExamSession;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.CourseRepository;
import school.hei.haapi.repository.RetakeExamSessionRepository;
import school.hei.haapi.repository.UserRepository;

@Component
@AllArgsConstructor
public class RetakeExamMapper {
  @Autowired CourseMapper courseMapper;

  @Autowired RetakeExamSessionMapper retakeExamSessionMapper;
  @Autowired UserRepository userRepository;
  @Autowired RetakeExamSessionRepository retakeExamSessionRepository;
  @Autowired CourseRepository courseRepository;

  public school.hei.haapi.model.RetakeExam toDomain(CrupdateRetakeExam crupdateRetakeExam) {
    assert crupdateRetakeExam.getStudentId() != null;
    User studentUser = userRepository.findById(crupdateRetakeExam.getStudentId()).orElse(null);
    assert crupdateRetakeExam.getSessionId() != null;
    RetakeExamSession retakeExamSession =
        retakeExamSessionRepository.findById(crupdateRetakeExam.getSessionId()).orElse(null);
    Course course = courseRepository.getCourseById(crupdateRetakeExam.getCourseId());
    return school.hei.haapi.model.RetakeExam.builder()
        .id(crupdateRetakeExam.getId())
        .student(studentUser)
        .retakeExamSession(retakeExamSession)
        .course(course)
        .build();
  }

  public RetakeExam toRest(school.hei.haapi.model.RetakeExam retakeExam) {
    return new RetakeExam()
        .id(retakeExam.getId())
        .course(courseMapper.toRest(retakeExam.getCourse()))
        .session(retakeExamSessionMapper.toRest(retakeExam.getRetakeExamSession()));
  }

  public school.hei.haapi.model.RetakeExam toDomain(RetakeExam retakeExam) {
    assert retakeExam.getCourse() != null;
    assert retakeExam.getSession() != null;
    return school.hei.haapi.model.RetakeExam.builder()
        .id(retakeExam.getId())
        .course(courseMapper.toDomain(retakeExam.getCourse()))
        .retakeExamSession(retakeExamSessionMapper.toDomain(retakeExam.getSession()))
        .build();
  }

  public List<RetakeExam> toRestList(List<school.hei.haapi.model.RetakeExam> retakeExams) {
    return retakeExams.stream().map(this::toRest).toList();
  }

  public List<school.hei.haapi.model.RetakeExam> toDoMainList(
      List<CrupdateRetakeExam> crupdateRetakeExams) {
    return crupdateRetakeExams.stream().map(this::toDomain).toList();
  }
}
