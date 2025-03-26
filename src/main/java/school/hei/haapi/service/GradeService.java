package school.hei.haapi.service;

import static java.util.stream.Collectors.toUnmodifiableList;

import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Grade;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.GradeRepository;
import school.hei.haapi.repository.dao.GradeDao;

@Service
@AllArgsConstructor
public class GradeService {
  private final GradeRepository gradeRepository;
  private final GradeDao gradeDao;

  public Grade getGradeByExamIdAndStudentId(String examId, String studentId) {
    return gradeRepository
        .getGradeByExamIdAndStudentIdAndAwardedCourseIdAndGroupId(examId, studentId)
        .orElseThrow(() -> new NotFoundException("Grade not found"));
  }

  public Grade getById(String id) {
    return gradeRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("grade with id " + id + " not found"));
  }

  public Grade crupdateParticipantGrade(Grade grade) {
    Optional<Grade> getGrade =
        gradeRepository.findByExamIdAndStudentId(
            grade.getExam().getId(), grade.getStudent().getId());
    if (getGrade.isPresent()) {
      Grade presentGrade = getGrade.get();
      presentGrade.setScore(grade.getScore());
      return gradeRepository.save(presentGrade);
    }
    return gradeRepository.save(grade);
  }

  public List<Grade> crupdateParticipantGrade(List<Grade> grades) {
    return grades.stream().map(this::crupdateParticipantGrade).collect(toUnmodifiableList());
  }

  public List<Grade> getParticipantsGradeForExam(
      String exam_id, PageFromOne page, BoundedPageSize pageSize) {
    if (page == null || pageSize == null) return gradeDao.getGradesByExamId(exam_id, null);
    Pageable pageable = PageRequest.of((page.getValue() - 1), pageSize.getValue());
    return gradeDao.getGradesByExamId(exam_id, pageable);
  }
}
