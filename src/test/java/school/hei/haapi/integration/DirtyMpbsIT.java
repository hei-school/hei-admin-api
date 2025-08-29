package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static school.hei.haapi.integration.MpbsIT.expectedMpbs1;
import static school.hei.haapi.integration.StudentIT.student1;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT2_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestUtils.fee1;
import static school.hei.haapi.integration.conf.TestUtils.setUpCasdoor;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.setUpEventBridge;
import static school.hei.haapi.integration.conf.TestUtils.setUpS3Service;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.api.PayingApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.Mpbs.Mpbs;
import school.hei.haapi.model.User;
import school.hei.haapi.service.MpbsService;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

@Testcontainers
@AutoConfigureMockMvc
class DirtyMpbsIT extends FacadeITMockedThirdParties {
  @MockBean private EventBridgeClient eventBridgeClientMock;
  @MockBean private MpbsService mpbsService;
  private final String mpbsId = expectedMpbs1().getId();

  @BeforeEach
  void setUp() {
    setUpCasdoor(casdoorAuthServiceMock, certificateLoaderMock);
    setUpCognito(cognitoComponentMock);
    setUpEventBridge(eventBridgeClientMock);
    setUpS3Service(fileService, student1());

    when(mpbsService.pendFailedMpbs(mpbsId))
        .thenReturn(
            Mpbs.builder()
                .id(mpbsId)
                .student(User.builder().id(student1().getId()).build())
                .fee(Fee.builder().id(fee1().getId()).build())
                .build());
  }

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  @Test
  void student1_pend_own_ok() {
    PayingApi api = new PayingApi(anApiClient(STUDENT1_TOKEN));
    when(mpbsService.findByIdAndStudentId(anyString(), anyString()))
        .thenReturn(Optional.of(Mpbs.builder().id(mpbsId).build()));

    assertDoesNotThrow(() -> api.pendFailedMpbs(mpbsId));
  }

  @Test
  void student2_pend_other_ko() {
    PayingApi api = new PayingApi(anApiClient(STUDENT2_TOKEN));
    when(mpbsService.findByIdAndStudentId(anyString(), anyString())).thenReturn(Optional.empty());

    assertThrowsForbiddenException(() -> api.pendFailedMpbs(mpbsId));
  }
}
