package school.hei.haapi.service;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.repository.ExamRepository;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Service
@AllArgsConstructor
public class ExamService {
  //todo: to review all class
  private final ExamRepository examRepository;
  public List<Exam> getExamsFromAwardedCourseIdAndGroupId(
      String groupId,
      String awardedCourseId,
      PageFromOne page,
      BoundedPageSize pageSize
  ) {
    Pageable pageable = PageRequest.of(
            page.getValue() - 1,
            pageSize.getValue(),
            Sort.by(DESC, "examinationDate"));
    return examRepository.findExamsByGroupIdAndAwardedGroupId(
        groupId,
        awardedCourseId,
        pageable
    ).toList();
  }

  public Exam getExamsByIdAndGroupIdAndAwardedCourseId(
      String id,
      String awardedCourseId,
      String groupId
  ) {
    return examRepository.findExamsByIdAndGroupIdAndAwardedGroupId(id, awardedCourseId, groupId);
  }

  public List<Exam> updateOrSaveAll(List<Exam> exams) {
    return examRepository.saveAll(exams);
  }
}
