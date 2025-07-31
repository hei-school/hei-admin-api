package school.hei.haapi.service;

import static org.springframework.data.domain.Sort.Direction.DESC;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.Grade;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.model.validator.ExamValidator;
import school.hei.haapi.repository.ExamRepository;
import school.hei.haapi.repository.GradeRepository;
import school.hei.haapi.repository.dao.ExamDao;

@Service
@AllArgsConstructor
public class ExamService {
  private final ExamRepository examRepository;
  private final ExamDao examDao;
  private final UserService userService;
  private final GradeService gradeService;
  private final GradeRepository gradeRepository;
  private final ExamValidator validator;

  public List<Exam> getExamsFromAwardedCourseIdAndGroupId(
      String groupId, String awardedCourseId, PageFromOne page, BoundedPageSize pageSize) {
    Pageable pageable =
        PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(DESC, "examinationDate"));
    return examRepository
        .findExamsByGroupIdAndCourseAssignmentId(groupId, awardedCourseId, pageable)
        .toList();
  }

  public Exam getExamsByIdAndGroupIdAndAwardedCourseId(
      String id, String awardedCourseId, String groupId) {
    return examRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Exam with id #" + id + " not found"));
  }

  public List<Exam> updateOrSaveAll(List<Exam> exams) {
    validator.accept(exams);
    List<Exam> savedExams = examRepository.saveAll(exams);
    List<Grade> gradesToInitialize = new ArrayList<>();
    savedExams.forEach(exam -> gradesToInitialize.addAll(initializeExamGrades(exam)));
    gradeService.crupdateParticipantGrade(gradesToInitialize);
    return savedExams;
  }

  private Grade initializeExamGrade(Exam exam, User user) {
    return gradeRepository
        .getGradeByExamIdAndStudentId(exam.getId(), user.getId())
        .orElse(new Grade(exam, user));
  }

  private List<Grade> initializeExamGrades(Exam exam) {
    return exam.getCourseAssignment().getGroups().stream()
        .map(group -> userService.getByGroupId(group.getId()))
        .flatMap(List::stream)
        .distinct()
        .map(user -> initializeExamGrade(exam, user))
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
