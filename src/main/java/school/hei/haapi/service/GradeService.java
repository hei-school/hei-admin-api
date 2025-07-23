package school.hei.haapi.service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.model.ExamGradeStats;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Grade;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.GradeRepository;
import school.hei.haapi.repository.dao.GradeDao;

@Slf4j
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

  @Transactional
  public Grade checkAndCreateOrModifyGrade(Grade grade) {
    Optional<Grade> getGrade =
        gradeRepository.findByExamIdAndStudentId(
            grade.getExam().getId(), grade.getStudent().getId());
    if (getGrade.isPresent()) {
      Grade presentGrade = getGrade.get();
      presentGrade.setScore(grade.getScore());
      return presentGrade;
    }
    Optional<Group> studentCurrentGroup = grade.getStudent().findCurrentGroup();
    // TODO: refactor this to be more readable
    if (studentCurrentGroup.isEmpty()) {
      throw new BadRequestException(
          String.format("Student with id: %s not in any group", grade.getStudent().getId()));
    }
    var isInAssignedGroups =
        grade.getExam().getCourseAssignment().getGroups().stream()
            .anyMatch(group -> studentCurrentGroup.get().getId().equals(group.getId()));
    if (!isInAssignedGroups) {
      throw new BadRequestException(
          String.format(
              "Student with id: %s not in the Exam: %s",
              grade.getStudent().getId(), grade.getExam().getId()));
    }
    return grade;
  }

  @Transactional
  public List<Grade> crupdateParticipantGrade(List<Grade> grades) {
    log.info("crupdateParticipantGrade: {}", grades);
    return gradeRepository.saveAll(grades.stream().map(this::checkAndCreateOrModifyGrade).toList());
  }

  public List<Grade> getParticipantsGradeForExam(
      String exam_id, PageFromOne page, BoundedPageSize pageSize) {
    return gradeDao.getGradesByExamId(
        exam_id,
        (page == null || pageSize == null)
            ? Pageable.unpaged()
            : PageRequest.of((page.getValue() - 1), pageSize.getValue()));
  }

  private double getExamAverageGrade(String examId) {
    var averageOfGradeResult =
        gradeDao.getGradesByExamId(examId).stream().mapToDouble(Grade::getScore).average();
    if (averageOfGradeResult.isEmpty())
      throw new NotFoundException("Exam with id " + examId + " do not have a score");
    return averageOfGradeResult.getAsDouble();
  }

  public ExamGradeStats getExamGradeStats(String examId) {
    return new ExamGradeStats().average(getExamAverageGrade(examId));
  }
}
