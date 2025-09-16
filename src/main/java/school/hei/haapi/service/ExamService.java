package school.hei.haapi.service;

import static org.springframework.data.domain.Sort.Direction.DESC;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.model.validator.ExamValidator;
import school.hei.haapi.repository.ExamRepository;
import school.hei.haapi.repository.dao.ExamDao;

@Service
@AllArgsConstructor
public class ExamService {
  private final ExamRepository examRepository;
  private final ExamDao examDao;
  private final ExamValidator validator;

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
      String teacherId,
      String groupRef,
      Instant examinationDateStart,
      Instant examinationDateEnd) {
    Pageable pageable =
        PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(DESC, "examinationDate"));
    return examDao.findByCriteria(
        pageable, title, courseCode, teacherId, groupRef, examinationDateStart, examinationDateEnd);
  }

  public List<Exam> getExamsByCourseId(String courseId) {
    return examRepository.findExamsByCourseId(courseId);
  }

  public List<Exam> getExamsByCourseAssignmentIds(List<String> courseAssignmentIds) {
    return examRepository.findExamsByCourseAssignmentIdIn(courseAssignmentIds);
  }
}
