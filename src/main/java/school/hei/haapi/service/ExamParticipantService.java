package school.hei.haapi.service;


import jakarta.transaction.Transactional;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.mapper.GradeMapper;
import school.hei.haapi.endpoint.rest.model.StudentGrade;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.pagination.PaginationFromPageAndPageSize;
import school.hei.haapi.repository.dao.GradeDao;

@Service
@AllArgsConstructor
public class ExamParticipantService {
  private final GradeMapper gradeMapper;
  private final GradeDao gradeDao;
  private final PaginationFromPageAndPageSize pageableFromPageAndSize;

  @Transactional
  public List<StudentGrade> getExamParticipantsGrade(
      String examId, PageFromOne page, BoundedPageSize pageSize, String studentRef) {
    return gradeDao
        .getGradesByExamId(examId, studentRef, pageableFromPageAndSize.apply(page, pageSize))
        .stream()
        .map(gradeMapper::toRestStudentGrade)
        .toList();
  }
}
