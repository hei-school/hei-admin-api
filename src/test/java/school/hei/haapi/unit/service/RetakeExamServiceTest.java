package school.hei.haapi.unit.service;

import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.FeeCategory.L1;
import static school.hei.haapi.endpoint.rest.model.FeeFrequency.UNKNOWN;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.LATE;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.UNPAID;
import static school.hei.haapi.endpoint.rest.model.FeeTypeEnum.RETAKE_EXAM_COSTS;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.mapper.GradeMapper;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.FeeTemplate;
import school.hei.haapi.model.RetakeExam;
import school.hei.haapi.model.RetakeExamSession;
import school.hei.haapi.model.RetakeExamStatus;
import school.hei.haapi.model.User;
import school.hei.haapi.model.pagination.PaginationFromPageAndPageSize;
import school.hei.haapi.repository.FeeTemplateRepository;
import school.hei.haapi.repository.RetakeExamFeeRepository;
import school.hei.haapi.repository.RetakeExamRepository;
import school.hei.haapi.repository.dao.RetakeExamDao;
import school.hei.haapi.service.FeeService;
import school.hei.haapi.service.GradeResultService;
import school.hei.haapi.service.GradeService;
import school.hei.haapi.service.RetakeExamService;
import school.hei.haapi.service.RetakeExamSessionService;

class RetakeExamServiceTest {
  private RetakeExamRepository retakeExamRepository;
  private FeeTemplateRepository feeTemplateRepository;
  private FeeService feeService;
  private RetakeExamService subject;

  @BeforeEach
  void setUp() {
    retakeExamRepository = mock();
    feeTemplateRepository = mock();
    feeService = mock();
    subject =
        new RetakeExamService(
            retakeExamRepository,
            mock(RetakeExamSessionService.class),
            mock(GradeResultService.class),
            mock(CourseMapper.class),
            mock(RetakeExamDao.class),
            mock(PaginationFromPageAndPageSize.class),
            mock(GradeService.class),
            mock(GradeMapper.class),
            feeTemplateRepository,
            feeService,
            mock(RetakeExamFeeRepository.class));
    when(retakeExamRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
  }

  private static User aStudent() {
    return User.builder().id("student_id").ref("STD21001").build();
  }

  private static RetakeExamSession aSession(Instant dateFrom) {
    return RetakeExamSession.builder().id("session_id").dateFrom(dateFrom).build();
  }

  private static RetakeExam aNewRetakeExam(RetakeExamSession session, User student) {
    return RetakeExam.builder()
        .session(session)
        .student(student)
        .course(Course.builder().id("course_id").build())
        .status(RetakeExamStatus.REGISTERED)
        .build();
  }

  private static FeeTemplate aRetakeExamFeeTemplate(int amount) {
    return FeeTemplate.builder()
        .id("template_id")
        .name("Retake exam fee")
        .amount(amount)
        .type(RETAKE_EXAM_COSTS)
        .category(L1)
        .frequency(UNKNOWN)
        .build();
  }

  @Test
  void a_new_retake_exam_is_charged_the_latest_retake_exam_fee_template() {
    var student = aStudent();
    var session = aSession(now().plus(10, DAYS));
    var retakeExam = aNewRetakeExam(session, student);
    when(feeTemplateRepository.findFirstByTypeOrderByCreationDatetimeDesc(RETAKE_EXAM_COSTS))
        .thenReturn(Optional.of(aRetakeExamFeeTemplate(50000)));
    when(feeService.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

    subject.crupdateRetakeExams(List.of(retakeExam));

    var captor = ArgumentCaptor.forClass(List.class);
    verify(feeService).saveAll(captor.capture());
    List<Fee> createdFees = captor.getValue();
    assertEquals(1, createdFees.size());
    var fee = createdFees.get(0);
    assertEquals(student, fee.getStudent());
    assertEquals(RETAKE_EXAM_COSTS, fee.getType());
    assertEquals(L1, fee.getCategory());
    assertEquals(UNKNOWN, fee.getFrequency());
    assertEquals(50000, fee.getTotalAmount());
    assertEquals(50000, fee.getRemainingAmount());
    assertEquals(session.getDateFrom(), fee.getDueDatetime());
  }

  @Test
  void no_fee_is_created_when_no_retake_exam_fee_template_exists() {
    var retakeExam = aNewRetakeExam(aSession(now().plus(10, DAYS)), aStudent());
    when(feeTemplateRepository.findFirstByTypeOrderByCreationDatetimeDesc(RETAKE_EXAM_COSTS))
        .thenReturn(Optional.empty());

    subject.crupdateRetakeExams(List.of(retakeExam));

    verify(feeService, never()).saveAll(any());
  }

  @Test
  void updating_an_existing_retake_exam_does_not_create_a_new_fee() {
    var existingRetakeExam =
        RetakeExam.builder()
            .id("existing_id")
            .session(aSession(now().plus(10, DAYS)))
            .student(aStudent())
            .course(Course.builder().id("course_id").build())
            .status(RetakeExamStatus.VALIDATE)
            .build();
    when(feeTemplateRepository.findFirstByTypeOrderByCreationDatetimeDesc(RETAKE_EXAM_COSTS))
        .thenReturn(Optional.of(aRetakeExamFeeTemplate(50000)));

    subject.crupdateRetakeExams(List.of(existingRetakeExam));

    verify(feeService, never()).saveAll(any());
  }

  @Test
  void a_retake_exam_session_starting_in_the_past_yields_a_late_fee() {
    var retakeExam = aNewRetakeExam(aSession(now().minus(5, DAYS)), aStudent());
    when(feeTemplateRepository.findFirstByTypeOrderByCreationDatetimeDesc(RETAKE_EXAM_COSTS))
        .thenReturn(Optional.of(aRetakeExamFeeTemplate(50000)));
    when(feeService.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

    subject.crupdateRetakeExams(List.of(retakeExam));

    var captor = ArgumentCaptor.forClass(List.class);
    verify(feeService).saveAll(captor.capture());
    assertEquals(LATE, ((List<Fee>) captor.getValue()).get(0).getStatus());
  }

  @Test
  void a_retake_exam_session_starting_in_the_future_yields_an_unpaid_fee() {
    var retakeExam = aNewRetakeExam(aSession(now().plus(5, DAYS)), aStudent());
    when(feeTemplateRepository.findFirstByTypeOrderByCreationDatetimeDesc(RETAKE_EXAM_COSTS))
        .thenReturn(Optional.of(aRetakeExamFeeTemplate(50000)));
    when(feeService.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

    subject.crupdateRetakeExams(List.of(retakeExam));

    var captor = ArgumentCaptor.forClass(List.class);
    verify(feeService).saveAll(captor.capture());
    assertEquals(UNPAID, ((List<Fee>) captor.getValue()).get(0).getStatus());
  }

  @Test
  void one_fee_is_created_per_new_retake_exam_in_the_same_batch() {
    var session = aSession(now().plus(10, DAYS));
    var firstStudent = aStudent();
    var secondStudent = User.builder().id("student_id_2").ref("STD21002").build();
    when(feeTemplateRepository.findFirstByTypeOrderByCreationDatetimeDesc(RETAKE_EXAM_COSTS))
        .thenReturn(Optional.of(aRetakeExamFeeTemplate(50000)));
    when(feeService.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

    subject.crupdateRetakeExams(
        List.of(aNewRetakeExam(session, firstStudent), aNewRetakeExam(session, secondStudent)));

    var captor = ArgumentCaptor.forClass(List.class);
    verify(feeService).saveAll(captor.capture());
    assertEquals(2, ((List<Fee>) captor.getValue()).size());
    assertTrue(
        ((List<Fee>) captor.getValue())
            .stream()
                .map(Fee::getStudent)
                .toList()
                .containsAll(List.of(firstStudent, secondStudent)));
  }
}
