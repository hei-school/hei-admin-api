package school.hei.haapi.service;

import static java.time.Instant.now;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.CourseResultStatus.VALIDATED;
import static school.hei.haapi.endpoint.rest.model.FileType.TRANSCRIPT;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L1;
import static school.hei.haapi.endpoint.rest.model.YearlyResultGenerationStatus.AVAILABLE;
import static school.hei.haapi.endpoint.rest.model.YearlyResultGenerationStatus.GENERATING;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.model.CourseResult;
import school.hei.haapi.file.bucket.BucketComponent;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.model.FileInfo;
import school.hei.haapi.model.User;
import school.hei.haapi.model.YearlyResultGenerationRequest;
import school.hei.haapi.repository.FileInfoRepository;
import school.hei.haapi.repository.YearlyResultGenerationRequestRepository;

public class TranscriptGenerationTest extends FacadeITMockedThirdParties {
  @Autowired GradeResultService subject;
  @Autowired YearlyResultGenerationRequestRepository yearlyResultGenerationRequestRepository;
  @MockBean CourseResultService courseResultServiceMock;
  @Autowired FileInfoRepository fileInfoRepository;
  @MockBean UserService userServiceMock;
  @MockBean BucketComponent bucketComponentMock;

  private static final String STUDENT_ID = "id";
  private static final String STUDENT_REF = "ref";
  private static final String TRANSCRIPT_FILENAME_FORMAT = "Bulletin - %s - %s";
  private static final String DUMMY_PRESIGNED_URL = "https://dummy.com";

  @Test
  void get_generated_transcript_ok() throws MalformedURLException {
    var savedAvailableFileInfo = fileInfoRepository.save(availableFileInfo());
    yearlyResultGenerationRequestRepository.saveAll(
        List.of(generatingRequest(), availableRequest(savedAvailableFileInfo)));

    when(courseResultServiceMock.getCourseResultsByStudentIdAndLevel(any(), any()))
        .thenReturn(List.of(mgt1CourseResult(), prog1CourseResult(), sys1CourseResult()));
    when(userServiceMock.getById(any()))
        .thenReturn(User.builder().id(STUDENT_ID).ref(STUDENT_REF).build());
    when(bucketComponentMock.presign(any(), any()))
        .thenReturn(URI.create(DUMMY_PRESIGNED_URL).toURL());
    var result = subject.getYearlyResultTranscript(STUDENT_ID, L1);
    assertEquals(AVAILABLE, result.getStatus());
    assertEquals(DUMMY_PRESIGNED_URL, result.getLink());
  }

  private static CourseResult mgt1CourseResult() {
    return new CourseResult()
        .status(VALIDATED)
        .course(new Course().code("MGT1").name("Mgt 1").totalHours(45).credits(3).level(L1))
        .weightedAverage(BigDecimal.valueOf(20));
  }

  private static CourseResult prog1CourseResult() {
    return new CourseResult()
        .status(VALIDATED)
        .course(new Course().code("PROG1").name("Prog 1").totalHours(42).credits(6).level(L1))
        .weightedAverage(BigDecimal.valueOf(10));
  }

  private static CourseResult sys1CourseResult() {
    return new CourseResult()
        .status(VALIDATED)
        .course(new Course().code("SYS1").name("Sys 1").totalHours(25).credits(1).level(L1))
        .weightedAverage(BigDecimal.valueOf(20));
  }

  private static YearlyResultGenerationRequest generatingRequest() {
    return YearlyResultGenerationRequest.builder()
        .status(GENERATING)
        .fileName(String.format(TRANSCRIPT_FILENAME_FORMAT, STUDENT_REF, L1))
        .datetime(Instant.parse("2024-05-10T00:00:00.000Z"))
        .build();
  }

  private static YearlyResultGenerationRequest availableRequest(FileInfo fileInfo) {
    return YearlyResultGenerationRequest.builder()
        .status(AVAILABLE)
        .fileInfo(fileInfo)
        .fileName(String.format(TRANSCRIPT_FILENAME_FORMAT, STUDENT_REF, L1))
        .datetime(now())
        .build();
  }

  private static FileInfo availableFileInfo() {
    return FileInfo.builder().fileType(TRANSCRIPT).filePath("dummy").build();
  }
}
