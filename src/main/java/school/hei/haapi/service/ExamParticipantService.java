package school.hei.haapi.service;

import static java.util.stream.Collectors.toSet;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.mapper.GradeMapper;
import school.hei.haapi.endpoint.rest.mapper.UserMapper;
import school.hei.haapi.endpoint.rest.model.StudentGrade;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.Grade;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.User;
import school.hei.haapi.model.pagination.PaginationFromPageAndPageSize;
import school.hei.haapi.repository.dao.GradeDao;

@Service
@AllArgsConstructor
public class ExamParticipantService {
  private final GradeMapper gradeMapper;
  private final UserMapper userMapper;
  private final GradeDao gradeDao;
  private final ExamService examService;
  private final UserService userService;
  private final PaginationFromPageAndPageSize pageableFromPageAndSize;

  private StudentGrade correspondingGradeForStudentIn(User user, List<Grade> existingGrades) {
    var correspondingGrade =
        existingGrades.stream()
            .filter(grade -> user.getId().equals(grade.getStudent().getId()))
            .findFirst();

    return new StudentGrade()
        .student(userMapper.toRestStudent(user))
        .grade(correspondingGrade.map(gradeMapper::toRest).orElse(null));
  }

  private List<User> getExamParticipants(Exam exam, PageFromOne page, BoundedPageSize pageSize) {
    return userService.getByGroupIds(
        exam.getCourseAssignment().getGroups().stream().map(Group::getId).collect(toSet()),
        (page == null || pageSize == null)
            ? Pageable.unpaged()
            : pageableFromPageAndSize.apply(page, pageSize));
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
