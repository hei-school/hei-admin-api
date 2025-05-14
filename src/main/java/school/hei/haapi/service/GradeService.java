package school.hei.haapi.service;

import static java.util.stream.Collectors.toUnmodifiableList;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.model.ExamGradeStats;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Grade;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.GradeRepository;
import school.hei.haapi.repository.dao.GradeDao;

@Service
@AllArgsConstructor
public class GradeService {
  private final GradeRepository gradeRepository;
  private final GradeDao gradeDao;
  private final UserService userService;

  public Grade getGradeByExamIdAndStudentId(String examId, String studentId) {
    return gradeRepository
        .getGradeByExamIdAndStudentId(examId, studentId)
        .orElseThrow(() -> new NotFoundException("Grade not found"));
  }

  public Grade getById(String id) {
    return gradeRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("grade with id " + id + " not found"));
  }

  private Grade checkAndCreateOrModifyGrade(Grade grade) {
    Optional<Grade> getGrade =
        gradeRepository.findByExamIdAndStudentId(
            grade.getExam().getId(), grade.getStudent().getId());
    if (getGrade.isPresent()) {
      Grade presentGrade = getGrade.get();
      presentGrade.setScore(grade.getScore());
      return presentGrade;
    }
    if (!userService
        .getByGroupId(grade.getExam().getAwardedCourse().getGroup().getId())
        .contains(grade.getStudent())) {
      throw new BadRequestException(
          String.format(
              "Student with id: %s not in the Exam: %s",
              grade.getStudent().getId(), grade.getExam().getId()));
    }
    return grade;
  }

  @Transactional
  public List<Grade> crupdateParticipantGrade(List<Grade> grades) {
    return gradeRepository.saveAll(
        grades.stream().map(this::checkAndCreateOrModifyGrade).collect(toUnmodifiableList()));
  }

  public List<Grade> getParticipantsGradeForExam(
      String exam_id, PageFromOne page, BoundedPageSize pageSize) {
    return gradeDao.getGradesByExamId(
        exam_id,
        (page == null || pageSize == null)
            ? Pageable.unpaged()
            : PageRequest.of((page.getValue() - 1), pageSize.getValue()));
  }

  private OptionalDouble getExamAverageGrade(String examId) {
    return gradeDao.getGradesByExamId(examId).stream().mapToDouble(Grade::getScore).average();
  }

  public ExamGradeStats getExamGradeStats(String examId) {
    return new ExamGradeStats().average(getExamAverageGrade(examId).orElse(0));
  }
}
