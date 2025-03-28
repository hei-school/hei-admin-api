package school.hei.haapi.service;

import static java.util.stream.Collectors.toUnmodifiableList;
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
import school.hei.haapi.model.exception.NotFoundException;
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

  public List<Exam> getExamsFromAwardedCourseIdAndGroupId(
      String groupId, String awardedCourseId, PageFromOne page, BoundedPageSize pageSize) {
    Pageable pageable =
        PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(DESC, "examinationDate"));
    return examRepository
        .findExamsByGroupIdAndAwardedGroupId(groupId, awardedCourseId, pageable)
        .toList();
  }

  public Exam getExamsByIdAndGroupIdAndAwardedCourseId(
      String id, String awardedCourseId, String groupId) {
    return examRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Exam with id #" + id + " not found"));
  }

  public List<Exam> updateOrSaveAll(List<Exam> exams) {
    List<Exam> examList = examRepository.saveAll(exams);
    List<Grade> gradeToInitialize = new ArrayList<>();
    examList.forEach(exam -> gradeToInitialize.addAll(initializeExamGrades(exam)));
    gradeService.crupdateParticipantGrade(gradeToInitialize);
    return examList;
  }

  private List<Grade> initializeExamGrades(Exam exam) {
    return userService.getByGroupId(exam.getAwardedCourse().getGroup().getId()).stream()
        .map(
            student ->
                gradeRepository
                    .getGradeByExamIdAndStudentId(exam.getId(), student.getId())
                    .orElse(new Grade(exam, student)))
        .collect(toUnmodifiableList());
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

  public Exam createOrUpdateExamsInfos(Exam exam) {
    return examRepository.save(exam);
  }
}
