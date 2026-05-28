package school.hei.haapi.service;

import static org.reflections.Reflections.log;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L3;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.M2;
import static school.hei.haapi.model.ResultOverviewStatus.VALIDATED;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.mapper.StudentResultOverviewMapper;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.Promotion;
import school.hei.haapi.model.ResultOverviewStatus;
import school.hei.haapi.model.StudentResultOverview;
import school.hei.haapi.model.User;
import school.hei.haapi.model.pagination.PaginationFromPageAndPageSize;
import school.hei.haapi.repository.StudentResultOverviewRepository;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.repository.dao.StudentResultOverviewDao;

@Service
@AllArgsConstructor
public class StudentResultOverviewService {
  private final PaginationFromPageAndPageSize paginationFromPageAndPageSize;
  private final StudentResultOverviewDao studentResultOverviewDao;
  private final StudentResultOverviewMapper studentResultOverviewMapper;
  private final StudentResultOverviewRepository studentResultOverviewRepository;
  private final PromotionService promotionService;
  private final UserRepository userRepository;
  private final GradeResultService gradeResultService;
  private final UserService userService;

  public List<school.hei.haapi.endpoint.rest.model.StudentResultOverview> getStudentResultOverviews(
      String promotionId,
      school.hei.haapi.model.ResultOverviewStatus status,
      PageFromOne page,
      BoundedPageSize pageSize) {
    log.info("promotionId = {}", promotionId);
    var pageable = paginationFromPageAndPageSize.apply(page, pageSize);
    var studentResultOverviews =
        studentResultOverviewDao.filterByCriteria(promotionId, status, pageable);
    log.info("students result overviews : " + studentResultOverviews);
    return studentResultOverviewMapper.toRestList(studentResultOverviews);
  }

  public List<StudentResultOverview> getStudentResultOverviewsToCrupdate() {
    var students = userRepository.findAllStudentNotDisabledWithGroupFlow();
    var promotions = promotionService.getPromotions(null, null, null, null, null);
    return students.stream()
        .map(
            student -> {
              var resultSummary = gradeResultService.getStudentResultSummary(student.getId());
              var graduationPromotion =
                  getStudentGraduationPromotion(
                      ResultOverviewStatus.valueOf(resultSummary.getStatus().toString()),
                      promotions,
                      student);
              log.info("graduation promotion : " + graduationPromotion.getName());
              return StudentResultOverview.builder()
                  .student(student)
                  .graduationPromotion(graduationPromotion)
                  .obtainedCredits(resultSummary.getObtainedCredits())
                  .weightedAverage(resultSummary.getWeightedAverage())
                  .status(ResultOverviewStatus.valueOf(resultSummary.getStatus().toString()))
                  .totalCredits(resultSummary.getTotalCredits())
                  .build();
            })
        .toList();
  }

  public Promotion getStudentGraduationPromotion(
      ResultOverviewStatus status, List<Promotion> promotions, User student) {

    var existingOverview =
        studentResultOverviewRepository.findStudentResultOverviewsByStudentId(student.getId());
    var studentActualLevel = userService.getStudentLevel(student.getId());
    var group = student.getGroupFlows().getLast().getGroup();
    var currentPromotion = getPromotionByGroup(promotions, group);

    if (studentActualLevel != L3 && studentActualLevel != M2) {
      return currentPromotion;
    } else if (status == VALIDATED) {
      return privilegeFromDb(existingOverview.orElse(null), currentPromotion);
    } else if (isAlumni(currentPromotion)) {
      log.info("current promotion" + currentPromotion);
      return getNextNonAlumniPromotion(promotions, currentPromotion);
    }

    return currentPromotion;
  }

  private Promotion privilegeFromDb(StudentResultOverview existingOverview, Promotion fallback) {
    return existingOverview != null ? existingOverview.getGraduationPromotion() : fallback;
  }

  private boolean isAlumni(Promotion promotion) {
    return promotion.getRef().toLowerCase().contains("alumni");
  }

  private Promotion getPromotionByGroup(List<Promotion> promotions, Group group) {
    return promotions.stream()
        .filter(p -> p.getGroups().contains(group))
        .findFirst()
        .orElseThrow(
            () ->
                new RuntimeException(
                    "The group with id :  " + group.getId() + "doesn't have a promotion"));
  }

  private Promotion getNextNonAlumniPromotion(
      List<Promotion> promotions, Promotion currentPromotion) {
    var currentIndex = promotions.indexOf(currentPromotion);
    if (currentIndex == -1) {
      throw new RuntimeException("Promotion with id: " + currentPromotion.getId() + " not found");
    }
    return promotions.subList(currentIndex + 1, promotions.size()).stream()
        .filter(p -> !isAlumni(p))
        .findFirst()
        .orElse(currentPromotion);
  }

  public List<StudentResultOverview> saveAll() {
    return studentResultOverviewRepository.saveAll(getStudentResultOverviewsToCrupdate());
  }
}
