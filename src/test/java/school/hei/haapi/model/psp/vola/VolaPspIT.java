package school.hei.haapi.model.psp.vola;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.UNPAID;
import static school.hei.haapi.endpoint.rest.model.MobileMoneyType.ORANGE_MONEY;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.SUCCESS;
import static school.hei.haapi.integration.StudentIT.student1;
import static school.hei.haapi.integration.conf.TestUtils.*;
import static school.hei.haapi.model.User.Role.STUDENT;
import static school.hei.haapi.model.User.Status.ENABLED;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import school.hei.haapi.endpoint.rest.model.FeeCategory;
import school.hei.haapi.endpoint.rest.model.FeeFrequency;
import school.hei.haapi.endpoint.rest.model.FeeTypeEnum;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.User;
import school.hei.haapi.model.mpbs.Mpbs;
import school.hei.haapi.model.mpbs.MpbsStatusHistory;
import school.hei.haapi.model.psp.vola.api.VolaPsp;
import school.hei.haapi.repository.FeeRepository;
import school.hei.haapi.repository.MpbsRepository;
import school.hei.haapi.repository.UserRepository;

class VolaPspIT extends FacadeITMockedThirdParties {
  @Autowired VolaPsp volaPsp;

  @Autowired MpbsRepository mpbsRepository;

  @Autowired FeeRepository feeRepository;

  @Autowired UserRepository userRepository;

  private Mpbs mpbs;

  private void setUpTestData() {
    var userInserted =
        userRepository.saveAll(
            List.of(
                User.builder()
                    .id(randomUUID().toString())
                    .address("dummy Adresse")
                    .email("tiavina.3@mail.hei.school")
                    .entranceDatetime(Instant.now())
                    .lastName("Woods")
                    .ref("OLIVIA_WOODS")
                    .role(STUDENT)
                    .status(ENABLED)
                    .build()));

    var feeInserted =
        feeRepository.saveAll(
            List.of(
                Fee.builder()
                    .id(randomUUID().toString())
                    .comment("Test fee")
                    .totalAmount(10000)
                    .remainingAmount(10000)
                    .student(userInserted.getFirst())
                    .creationDatetime(Instant.now())
                    .status(UNPAID)
                    .updatedAt(Instant.now())
                    .dueDatetime(Instant.now())
                    .isDeleted(false)
                    .type(FeeTypeEnum.TUITION)
                    .category(FeeCategory.L1)
                    .frequency(FeeFrequency.MONTHLY)
                    .build()));

    var mpbsInserted =
        Mpbs.builder()
            .pspId("MP250917.1604.D33118")
            .status(SUCCESS)
            .fee(feeInserted.getFirst())
            .student(userInserted.getFirst())
            .mobileMoneyType(ORANGE_MONEY)
            .statusHistory(new ArrayList<MpbsStatusHistory>())
            .build();

    mpbs = mpbsRepository.save(mpbsInserted);
  }

  @BeforeEach
  void setUp() {
    setUpCasdoor(casdoorAuthServiceMock, certificateLoaderMock);
    setUpCognito(cognitoComponentMock);
    setUpS3Service(fileService, student1());
    setUpTestData();
  }

  @Test
  void read_succeeded_payment() {

    var verifiedMpbs = volaPsp.get(mpbs);

    assertEquals(SUCCESS, verifiedMpbs.getStatus());
  }
}
