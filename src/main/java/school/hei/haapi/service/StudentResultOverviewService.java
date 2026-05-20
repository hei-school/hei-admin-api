package school.hei.haapi.service;

import static org.reflections.Reflections.log;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L3;
import static school.hei.haapi.model.ResultOverviewStatus.VALIDATED;

import java.util.List;
import java.util.stream.IntStream;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.mapper.StudentResultOverviewMapper;
import school.hei.haapi.endpoint.rest.model.YearlyResult;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.Promotion;
import school.hei.haapi.model.ResultOverviewStatus;
import school.hei.haapi.model.StudentResultOverview;
import school.hei.haapi.model.User;
import school.hei.haapi.model.pagination.PaginationFromPageAndPageSize;
import school.hei.haapi.repository.StudentResultOverviewRepository;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.repository.YearlyResultGenerationRequestRepository;
import school.hei.haapi.repository.dao.StudentResultOverviewDao;

@Service
@AllArgsConstructor
public class StudentResultOverviewService {
  private final PaginationFromPageAndPageSize paginationFromPageAndPageSize;
  private final StudentResultOverviewDao studentResultOverviewDao;
  private final StudentResultOverviewMapper studentResultOverviewMapper;
  private final StudentResultOverviewRepository studentResultOverviewRepository;
  private final PromotionService promotionService;
  private final YearlyResultGenerationRequestRepository yearlyResultGenerationRequestRepository;
  private final UserRepository userRepository;
  private final GradeResultService gradeResultService;

  public List<school.hei.haapi.endpoint.rest.model.StudentResultOverview> getStudentResultOverviews(
      String promotionId,
      school.hei.haapi.model.ResultOverviewStatus status,
      PageFromOne page,
      BoundedPageSize pageSize) {
    var pageable = paginationFromPageAndPageSize.apply(page, pageSize);
    var studentResultOverviews =
        studentResultOverviewDao.filteryByCriteria(promotionId, status, pageable);
    return studentResultOverviewMapper.toRestList(studentResultOverviews);
  }

  public List<StudentResultOverview> getStudentResultOverviewsToCrupdate() {
    var students = userRepository.findAllStudentNotDisabled();
    var promotions = promotionService.getPromotions(null, null, null, null, null);
    return students.stream()
        .map(
            student -> {
              var yearlyResult =
                  gradeResultService.getLeveledYearlyResultByStudentId(L3, student.getId());
              log.info(
                  "student with "
                      + student.getRef()
                      + " yearly results : "
                      + yearlyResult.toString());
              var promotion = getStudentPromotion(yearlyResult, promotions, student);
              log.info("actual promotion : " + promotion.getName());
              return StudentResultOverview.builder()
                  .student(student)
                  .promotion(promotion)
                  .obtainedCredits(yearlyResult.getObtainedCredits())
                  .weightedAverage(yearlyResult.getWeightedAverage())
                  .status(ResultOverviewStatus.valueOf(yearlyResult.getStatus().toString()))
                  .totalCredits(yearlyResult.getTotalCredits())
                  .build();
            })
        .toList();
  }

  public Promotion getStudentPromotion(
      YearlyResult yearlyResult, List<Promotion> promotions, User student) {
    var group = student.getGroupFlows().getLast();
    int promotionIndex =
        IntStream.range(0, promotions.size())
            .filter(i -> promotions.get(i).getGroups().contains(group))
            .findFirst()
            .orElse(-1);
    if (yearlyResult.getStatus().equals(VALIDATED)) {
      return promotions.get(promotionIndex);
    }
    return promotions.get(promotionIndex + 1);
  }

  public List<StudentResultOverview> saveAll() {
    return studentResultOverviewRepository.saveAll(getStudentResultOverviewsToCrupdate());
  }
}
