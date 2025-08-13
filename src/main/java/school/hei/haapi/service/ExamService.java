package school.hei.haapi.service;

import static org.springframework.data.domain.Sort.Direction.DESC;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.mapper.GradeMapper;
import school.hei.haapi.endpoint.rest.mapper.UserMapper;
import school.hei.haapi.endpoint.rest.model.StudentGrade;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.Grade;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.model.validator.ExamValidator;
import school.hei.haapi.repository.ExamRepository;
import school.hei.haapi.repository.dao.ExamDao;
import school.hei.haapi.repository.dao.GradeDao;

@Service
@AllArgsConstructor
public class ExamService {
  private final ExamRepository examRepository;
  private final ExamDao examDao;
  private final UserService userService;
  private final ExamValidator validator;
  private final GradeDao gradeDao;
  private final GradeMapper gradeMapper;
  private final UserMapper userMapper;

  public List<Exam> getExamsFromAwardedCourseIdAndGroupId(
      String groupId, String awardedCourseId, PageFromOne page, BoundedPageSize pageSize) {
    Pageable pageable =
        PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(DESC, "examinationDate"));
    return examRepository
        .findExamsByGroupIdAndCourseAssignmentId(groupId, awardedCourseId, pageable)
        .toList();
  }

  public List<Exam> updateOrSaveAll(List<Exam> exams) {
    validator.accept(exams);
    return examRepository.saveAll(exams);
  }

  public List<StudentGrade> getParticipantsGradeForExam(
      String examId, PageFromOne page, BoundedPageSize pageSize) {
    List<Grade> existingGrades =
        gradeDao.getGradesByExamId(
            examId,
            (page == null || pageSize == null)
                ? Pageable.unpaged()
                : PageRequest.of((page.getValue() - 1), pageSize.getValue()));
    var exam = getExamById(examId);

    return getExamParticipants(exam).stream()
        .map(user -> correspondingGradeForStudentIn(user, existingGrades))
        .toList();
  }

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

  private List<User> getExamParticipants(Exam exam) {
    return exam.getCourseAssignment().getGroups().stream()
        .flatMap(group -> userService.getByGroupId(group.getId()).stream())
        .toList();
  }

  public Exam getExamById(String id) {
    return examRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Exam with id #" + id + " not found"));
  }

  public List<Exam> getAllExams(
      PageFromOne page,
      BoundedPageSize pageSize,
      String title,
      String courseCode,
      String groupRef,
      Instant examinationDateStart,
      Instant examinationDateEnd,
      String awardedCourseId) {
    Pageable pageable =
        PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(DESC, "examinationDate"));
    return examDao.findByCriteria(
        pageable,
        title,
        courseCode,
        groupRef,
        examinationDateStart,
        examinationDateEnd,
        awardedCourseId);
  }

  public List<Exam> getExamsByCourseId(String courseId) {
    return examRepository.findExamsByCourseId(courseId);
  }

  public List<Exam> getExamsByCourseAssignmentId(String courseId) {
    return examRepository.findExamsByCourseId(courseId);
  }
}
