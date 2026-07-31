package school.hei.haapi.integration;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.api.PayingApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.mapper.FeeMapper;
import school.hei.haapi.endpoint.rest.model.FeeStatusEnum;
import school.hei.haapi.endpoint.rest.model.FeeTypeEnum;
import school.hei.haapi.file.bucket.BucketComponent;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.Payment;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.FeeRepository;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.repository.dao.FeeDao;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static school.hei.haapi.endpoint.rest.model.FeeFrequency.MONTHLY;
import static school.hei.haapi.endpoint.rest.model.Payment.TypeEnum.CREDIT;
import static school.hei.haapi.integration.conf.TestUtils.ADMIN1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.setUpCasdoor;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.model.PaymentStatus.CREATED;
import static school.hei.haapi.model.PaymentStatus.VALIDATE;
import static school.hei.haapi.model.User.Role.STUDENT;

@Testcontainers
@AutoConfigureMockMvc
@Slf4j
class CreditControllerIT extends FacadeITMockedThirdParties {
  @Autowired EntityManager entityManager;
  @Autowired FeeRepository feeRepository;
  @Autowired private FeeMapper feeMapper;
  @Autowired UserRepository userRepository;
  @MockBean private BucketComponent bucketComponent;
  @Autowired FeeDao feeDao;
  private static User student;
  private static Fee feeToArchive;

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  @BeforeEach
  void setUp() {
    setUpCasdoor(casdoorAuthServiceMock, certificateLoaderMock);
    setUpCognito(cognitoComponentMock);
  }

  @BeforeEach
  void setUpTestData() {
    student = userRepository.save(student());
    var savedFees = feeRepository.saveAll(List.of(feeToArchive(), currentFee()));
    feeToArchive = savedFees.getFirst();
  }

  @Test
  void getCreditByStudentId() throws ApiException {
    var anApiClient = anApiClient(ADMIN1_TOKEN);
    var payingApi = new PayingApi(anApiClient);
    var archivedFee = payingApi.archiveStudentFee(student.getId(), feeToArchive.getId());
    assertNotNull(archivedFee);
  }

  @Test
  void getCreditTransactionsByStudentId() {}

  @AfterEach
  void tearDown() {}

  private static User student() {
    return User.builder()
        .ref("STD0001")
        .firstName("John")
        .lastName("Doe")
        .status(User.Status.ENABLED)
        .email("john.doe@gmail.com")
        .entranceDatetime(Instant.parse("2025-11-15T00:00:00Z"))
        .role(STUDENT)
        .build();
  }

  private static Fee feeToArchive() {
    return Fee.builder()
        .student(student)
        .status(FeeStatusEnum.PAID)
        .type(FeeTypeEnum.TUITION)
        .totalAmount(200_000)
        .remainingAmount(0)
        .dueDatetime(Instant.parse("2025-12-15T00:00:00Z"))
        .isArchived(false)
        .frequency(MONTHLY)
        .mobilePayments(List.of())
        .build();
  }

  private static Fee currentFee() {
    return Fee.builder()
        .id("fee-2")
        .student(student)
        .status(FeeStatusEnum.PENDING)
        .type(FeeTypeEnum.TUITION)
        .totalAmount(150_000)
        .remainingAmount(150_000)
        .dueDatetime(Instant.parse("2026-02-01T00:00:00Z"))
        .isArchived(false)
        .frequency(MONTHLY)
        .build();
  }

  private static Payment bankPayment() {
    return Payment.builder()
        .id("payment-1")
        .fee(feeToArchive())
        .type(school.hei.haapi.endpoint.rest.model.Payment.TypeEnum.BANK_TRANSFER)
        .status(VALIDATE)
        .amount(200_000)
        .comment("Bank payment")
        .creationDatetime(Instant.parse("2025-12-10T10:00:00Z"))
        .build();
  }

  private static Payment creditPaymentCreated() {
    return Payment.builder()
        .id("payment-2")
        .fee(currentFee())
        .type(CREDIT)
        .status(CREATED)
        .amount(50_000)
        .comment("Waiting manager validation")
        .creationDatetime(Instant.parse("2026-01-10T09:00:00Z"))
        .build();
  }

  private static Payment creditPaymentValidated() {
    return Payment.builder()
        .id("payment-2")
        .fee(currentFee())
        .type(CREDIT)
        .status(VALIDATE)
        .amount(50_000)
        .comment("Validated by manager")
        .creationDatetime(Instant.parse("2026-01-11T14:00:00Z"))
        .build();
  }
}
