package school.hei.haapi.service;

import static java.time.Instant.now;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L3;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.M2;
import static school.hei.haapi.model.ResultOverviewStatus.VALIDATED;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.mapper.StudentResultOverviewMapper;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.Promotion;
import school.hei.haapi.model.ResultOverviewStatus;
import school.hei.haapi.model.StudentResultOverview;
import school.hei.haapi.model.User;
import school.hei.haapi.model.pagination.PaginationFromPageAndPageSize;
import school.hei.haapi.model.promotion.PromotionLevelOutOfRangeException;
import school.hei.haapi.repository.StudentResultOverviewRepository;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.repository.dao.StudentResultOverviewDao;

@Slf4j
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

  public List<school.hei.haapi.endpoint.rest.model.StudentResultOverview> getStudentResultOverviews(
      String promotionId,
      school.hei.haapi.model.ResultOverviewStatus status,
      PageFromOne page,
      BoundedPageSize pageSize) {
    var pageable = paginationFromPageAndPageSize.apply(page, pageSize);
    var studentResultOverviews =
        studentResultOverviewDao.filterByCriteria(promotionId, status, pageable);
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
    StudentLevel studentActualLevel;
    var currentGroup =
        student
            .findCurrentGroup()
            .orElseThrow(
                () ->
                    new RuntimeException(
                        "The student with id : " + student.getId() + " doesn't have any group"));
    var existingOverview =
        studentResultOverviewRepository.findStudentResultOverviewsByStudentId(student.getId());
    var currentPromotion =
        Optional.ofNullable(currentGroup.getPromotion())
            .orElseThrow(
                () ->
                    new RuntimeException(
                        "The group with id : "
                            + currentGroup.getId()
                            + "doesn't have any promotion"));
    try {
      studentActualLevel = currentPromotion.getLevelAt(now());
    } catch (PromotionLevelOutOfRangeException e) {
      studentActualLevel = L3;
    }
    if (studentActualLevel != L3 && studentActualLevel != M2) {
      return currentPromotion;
    } else if (status == VALIDATED) {
      return privilegeFromDb(existingOverview.orElse(null), currentPromotion);
    } else if (isAlumni(currentPromotion)) {
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

  private Promotion getNextNonAlumniPromotion(
      List<Promotion> promotions, Promotion currentPromotion) {
    var currentIndex =
        IntStream.range(0, promotions.size())
            .filter(i -> promotions.get(i).getName().equals(currentPromotion.getName()))
            .findFirst()
            .orElse(-1);
    if (currentIndex == -1) {
      throw new RuntimeException(
          "Promotion with name : " + currentPromotion.getName() + " not found");
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
