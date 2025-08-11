package school.hei.haapi.service;

import static java.math.BigDecimal.ZERO;
import static java.math.MathContext.UNLIMITED;
import static java.time.temporal.ChronoUnit.MINUTES;
import static school.hei.haapi.endpoint.rest.model.FileType.TRANSCRIPT;
import static school.hei.haapi.endpoint.rest.model.ResultOverviewStatus.INVALIDATED;
import static school.hei.haapi.endpoint.rest.model.ResultOverviewStatus.IN_PROGRESS;
import static school.hei.haapi.endpoint.rest.model.ResultOverviewStatus.VALIDATED;
import static school.hei.haapi.endpoint.rest.model.YearlyResultGenerationStatus.AVAILABLE;
import static school.hei.haapi.endpoint.rest.model.YearlyResultGenerationStatus.GENERATING;
import static school.hei.haapi.service.utils.FileUtils.multipartFileFromFile;

import java.io.File;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.model.YearlyResultTranscriptGeneration;
import school.hei.haapi.endpoint.rest.model.*;
import school.hei.haapi.file.bucket.BucketComponent;
import school.hei.haapi.model.FileInfo;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.exception.CoursesCreditSumZero;

@Service
@AllArgsConstructor
@Slf4j
public class GradeResultService {
  private final CourseResultService courseResultService;
  private final YearlyResultGenerationService yearlyResultGenerationService;
  private final BucketComponent bucketComponent;
  private final UserService userService;
  private final FileInfoService fileInfoService;
  private final EventProducer<YearlyResultTranscriptGeneration> eventProducer;
  private static final String TRANSCRIPT_FILENAME_FORMAT = "Bulletin - %s - %s";

  public YearlyResult getLeveledYearlyResultByStudentId(StudentLevel level, String studentId) {
    var courseResults = courseResultService.courseResultsForLevelOfStudent(level, studentId);

    return new YearlyResult()
        .level(level)
        .weightedAverage(courseResultService.weightedSumOfCourseResults(courseResults))
        .obtainedCredits(courseResultService.obtainedCreditsOfCourseResults(courseResults))
        .courseResults(courseResults)
        .status(courseResultService.courseValidationFromCourseResult(courseResults))
        .totalCredits(BigDecimal.valueOf(courseResultService.getSumCredits(courseResults)));
  }

  private Optional<YearlyResult> findLeveledYearlyResultByStudentId(
      StudentLevel level, String studentId) {
    try {
      return Optional.of(getLeveledYearlyResultByStudentId(level, studentId));
    } catch (CoursesCreditSumZero e) {
      log.error(
          "Course results for the level {} of the student id {} coefficient sum is 0",
          level,
          studentId,
          e);
      return Optional.empty();
    }
  }

  public ResultSummary getStudentResultSummary(String studentId) {
    List<YearlyResult> yearlyResultList =
        Arrays.stream(StudentLevel.values())
            .map(level -> findLeveledYearlyResultByStudentId(level, studentId))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();

    BigDecimal obtainedCredits =
        yearlyResultList.stream()
            .map(YearlyResult::getObtainedCredits)
            .filter(Objects::nonNull)
            .reduce(BigDecimal::add)
            .orElse(ZERO);

    List<BigDecimal> yearlyResults =
        yearlyResultList.stream()
            .map(YearlyResult::getWeightedAverage)
            .filter(Objects::nonNull)
            .toList();

    BigDecimal yearlyResultsWeightedAverageSum =
        yearlyResults.stream().reduce(BigDecimal::add).orElse(ZERO);

    BigDecimal weightedAverage =
        yearlyResultsWeightedAverageSum.divide(BigDecimal.valueOf(yearlyResults.size()), UNLIMITED);

    return new ResultSummary()
        .yearlyResults(yearlyResultList)
        .obtainedCredits(obtainedCredits)
        .weightedAverage(weightedAverage)
        .status(resultSummaryStatusFromYearlyResults(yearlyResultList))
        .totalCredits(
            yearlyResultList.parallelStream()
                .map(YearlyResult::getTotalCredits)
                .reduce(ZERO, BigDecimal::add));
  }

  private ResultOverviewStatus resultSummaryStatusFromYearlyResults(
      List<YearlyResult> yearlyResultList) {
    var yearlyResultsStatus = yearlyResultList.stream().map(YearlyResult::getStatus).toList();

    if (yearlyResultsStatus.stream().anyMatch(IN_PROGRESS::equals)) {
      return IN_PROGRESS;
    } else if (yearlyResultsStatus.stream().allMatch(VALIDATED::equals)) {
      return VALIDATED;
    } else {
      return INVALIDATED;
    }
  }

  public YearlyResultGenerationTranscript getYearlyResultTranscript(
      String studentId, StudentLevel level) {
    YearlyResult studentYearlyResult = getLeveledYearlyResultByStudentId(level, studentId);
    if (IN_PROGRESS.equals(studentYearlyResult.getStatus())) {
      throw new BadRequestException(
          "Cannot generate transcript for this level. This level is not yet completed");
    }

    User student = userService.findById(studentId);
    var fileName = String.format(TRANSCRIPT_FILENAME_FORMAT, student.getRef(), level);
    Optional<FileInfo> studentTranscriptFileInfo =
        fileInfoService.findTranscriptInfoByName(fileName);
    if (studentTranscriptFileInfo.isPresent()) {
      var presignedTranscriptUrl =
          bucketComponent.presign(
              studentTranscriptFileInfo.get().getFilePath(), Duration.of(10, MINUTES));
      return new YearlyResultGenerationTranscript()
          .status(AVAILABLE)
          .link(presignedTranscriptUrl.toString());
    }

    eventProducer.accept(
        List.of(
            YearlyResultTranscriptGeneration.builder()
                .user(student)
                .yearlyResult(studentYearlyResult)
                .build()));
    return new YearlyResultGenerationTranscript().status(GENERATING);
  }

  public void uploadYearlyResultTranscript(User student, YearlyResult yearlyResult) {
    File yearlyResultTranscript =
        yearlyResultGenerationService.generateYearlyResultTranscript(student, yearlyResult);
    String fileName =
        String.format(TRANSCRIPT_FILENAME_FORMAT, student.getRef(), yearlyResult.getLevel());
    String transcriptKey = fileName + ".pdf";
    bucketComponent.upload(yearlyResultTranscript, transcriptKey);
    fileInfoService.uploadFile(
        fileName, TRANSCRIPT, student.getId(), multipartFileFromFile(yearlyResultTranscript));
  }
}
