package school.hei.haapi.service;

import static java.math.BigDecimal.ZERO;
import static java.math.MathContext.DECIMAL128;
import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.MINUTES;
import static school.hei.haapi.endpoint.rest.model.FileType.TRANSCRIPT;
import static school.hei.haapi.endpoint.rest.model.ResultOverviewStatus.INVALIDATED;
import static school.hei.haapi.endpoint.rest.model.ResultOverviewStatus.IN_PROGRESS;
import static school.hei.haapi.endpoint.rest.model.ResultOverviewStatus.NOT_STARTED;
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
import school.hei.haapi.endpoint.rest.model.ResultOverviewStatus;
import school.hei.haapi.endpoint.rest.model.ResultSummary;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.endpoint.rest.model.YearlyResult;
import school.hei.haapi.endpoint.rest.model.YearlyResultGenerationTranscript;
import school.hei.haapi.file.bucket.BucketComponent;
import school.hei.haapi.model.User;
import school.hei.haapi.model.YearlyResultGenerationRequest;
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
  private static final Duration TRANSCRIPT_GENERATION_TIMEOUT = Duration.ofMinutes(5);

  public YearlyResult getLeveledYearlyResultByStudentId(StudentLevel level, String studentId) {
    var courseResults = courseResultService.getCourseResultsForLevelOfStudent(level, studentId);
    var yearlyResult = new YearlyResult().level(level);

    if (courseResults.isEmpty()) return yearlyResult.status(NOT_STARTED);

    return yearlyResult
        .weightedAverage(courseResultService.weightedSumOfCourseResults(courseResults).orElse(null))
        .obtainedCredits(courseResultService.obtainedCreditsOfCourseResults(courseResults))
        .courseResults(courseResults)
        .status(courseResultService.courseValidationFromCourseResult(courseResults).orElse(null))
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
    List<YearlyResult> yearlyResults =
        Arrays.stream(StudentLevel.values())
            .map(level -> findLeveledYearlyResultByStudentId(level, studentId))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();
    List<YearlyResult> yearlyResultsDone =
        yearlyResults.stream()
            .filter(yearlyResult -> !NOT_STARTED.equals(yearlyResult.getStatus()))
            .toList();

    BigDecimal obtainedCredits =
        yearlyResultsDone.stream()
            .map(YearlyResult::getObtainedCredits)
            .filter(Objects::nonNull)
            .reduce(BigDecimal::add)
            .orElse(ZERO);

    var weightedAverage =
        getWeightedAverageFromYearlyResultValues(
            yearlyResultsDone.stream()
                .map(YearlyResult::getWeightedAverage)
                .filter(Objects::nonNull)
                .toList());

    return new ResultSummary()
        .yearlyResults(yearlyResults)
        .obtainedCredits(obtainedCredits)
        .weightedAverage(weightedAverage.orElse(null))
        .status(resultSummaryStatusFromYearlyResults(yearlyResultsDone))
        .totalCredits(
            yearlyResultsDone.parallelStream()
                .map(YearlyResult::getTotalCredits)
                .reduce(ZERO, BigDecimal::add));
  }

  private static Optional<BigDecimal> getWeightedAverageFromYearlyResultValues(
      List<BigDecimal> yearlyResults) {
    if (yearlyResults.isEmpty()) return Optional.empty();

    return Optional.of(
        yearlyResults.stream()
            .reduce(ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(yearlyResults.size()), DECIMAL128));
  }

  private static ResultOverviewStatus resultSummaryStatusFromYearlyResults(
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
    var studentYearlyResult = getLeveledYearlyResultByStudentId(level, studentId);

    if (NOT_STARTED.equals(studentYearlyResult.getStatus()))
      throw new BadRequestException(
          "Cannot generate transcript for this level. This level has not yet been started");

    User student = userService.findById(studentId);
    var fileName = String.format(TRANSCRIPT_FILENAME_FORMAT, student.getRef(), level);
    Optional<YearlyResultGenerationRequest> studentTranscriptRequestInfo =
        yearlyResultGenerationService.findGenerationRequestByFileName(fileName);
    if (studentTranscriptRequestInfo.isPresent()) {
      var request = studentTranscriptRequestInfo.get();
      if (AVAILABLE.equals(request.getStatus())) {
        var presignedTranscriptUrl =
            bucketComponent.presign(request.getFileInfo().getFilePath(), Duration.of(10, MINUTES));
        return new YearlyResultGenerationTranscript()
            .status(AVAILABLE)
            .link(presignedTranscriptUrl.toString());
      }
      if (Duration.between(request.getDatetime(), now())
          .minus(TRANSCRIPT_GENERATION_TIMEOUT)
          .isPositive()) {
        generateTranscript(studentId, studentYearlyResult);
      }
    }
    return new YearlyResultGenerationTranscript().status(GENERATING);
  }

  private void generateTranscript(String studentId, YearlyResult studentYearlyResult) {
    eventProducer.accept(
        List.of(
            YearlyResultTranscriptGeneration.builder()
                .userId(studentId)
                .yearlyResult(studentYearlyResult)
                .build()));
  }

  public void uploadYearlyResultTranscript(String studentId, YearlyResult yearlyResult) {
    User student = userService.findById(studentId);
    String fileName =
        String.format(TRANSCRIPT_FILENAME_FORMAT, student.getRef(), yearlyResult.getLevel());
    yearlyResultGenerationService.saveGenerationRequest(
        YearlyResultGenerationRequest.builder()
            .datetime(now())
            .fileName(fileName)
            .status(GENERATING)
            .build());
    File yearlyResultTranscript =
        yearlyResultGenerationService.generateYearlyResultTranscript(student, yearlyResult);
    String transcriptKey = fileName + ".pdf";
    bucketComponent.upload(yearlyResultTranscript, transcriptKey);
    var uploadedTranscriptFileInfo =
        fileInfoService.uploadFile(
            fileName, TRANSCRIPT, student.getId(), multipartFileFromFile(yearlyResultTranscript));
    yearlyResultGenerationService.saveGenerationRequest(
        YearlyResultGenerationRequest.builder()
            .datetime(now())
            .fileName(fileName)
            .status(AVAILABLE)
            .fileInfo(uploadedTranscriptFileInfo)
            .build());
  }
}
