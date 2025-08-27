package school.hei.haapi.service;

import java.util.List;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.mapper.GradeMapper;
import school.hei.haapi.endpoint.rest.mapper.UserMapper;
import school.hei.haapi.endpoint.rest.model.StudentGrade;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.Grade;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.dao.GradeDao;

@Service
@AllArgsConstructor
public class ExamParticipantService {
  private final GradeMapper gradeMapper;
  private final UserMapper userMapper;
  private final GradeDao gradeDao;
  private final ExamService examService;
  private final UserService userService;

  private StudentGrade correspondingGradeForStudentIn(User user, List<Grade> existingGrades) {
    var correspondingGrade =
        existingGrades.stream()
            .filter(grade -> user.getId().equals(grade.getStudent().getId()))
            .findFirst();

    var studentGrade = new StudentGrade();
    studentGrade.setStudent(userMapper.toRestStudent(user));
    correspondingGrade.ifPresent(grade -> studentGrade.setGrade(gradeMapper.toRest(grade)));
    return studentGrade;
  }

  /**
   * Todo: performance issues, create something to not fetch all student for each group before the
   * pagination
   */
  private List<User> getExamParticipants(Exam exam, PageFromOne page, BoundedPageSize pageSize) {
    Stream<User> examParticipants =
        exam.getCourseAssignment().getGroups().stream()
            .flatMap(group -> userService.getByGroupId(group.getId()).stream());

    return (page == null || pageSize == null)
        ? examParticipants.toList()
        : examParticipants
            .skip((long) (page.getValue() - 1) * pageSize.getValue())
            .limit(pageSize.getValue())
            .toList();
  }

  public List<StudentGrade> getParticipantsGradeForExam(
      String examId, PageFromOne page, BoundedPageSize pageSize) {
    List<Grade> existingGrades = gradeDao.getGradesByExamId(examId);
    var exam = examService.getExamById(examId);

    return getExamParticipants(exam, page, pageSize).stream()
        .map(user -> correspondingGradeForStudentIn(user, existingGrades))
        .toList();
  }
}
