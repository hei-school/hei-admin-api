package school.hei.haapi.unit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.FAILED;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.PENDING;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.SUCCESS;
import static school.hei.haapi.integration.conf.TestUtils.getMockedFile;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import school.hei.haapi.model.MobileTransactionDetails;
import school.hei.haapi.repository.MpbsRepository;
import school.hei.haapi.service.MobilePaymentService;
import school.hei.haapi.service.MpbsVerificationService;
import school.hei.haapi.service.utils.CollectionUtils;

class VerificationMpbsByXlsxTest {
  private final MpbsRepository mockedMpbsRepository = mock();
  private final MobilePaymentService mobilePaymentService = mock();
  private final MpbsVerificationService subject =
      new MpbsVerificationService(
          mock(),
          mockedMpbsRepository,
          mobilePaymentService,
          mock(),
          mock(),
          mock(),
          mock(),
          mock(),
          new CollectionUtils());

  private static List<MobileTransactionDetails> excelTransactionDetails() {
    return List.of(
        MobileTransactionDetails.builder()
            .pspTransactionRef("MP241213.0844.B33334")
            .pspTransactionAmount(330000)
            .status(FAILED)
            .build(),
        MobileTransactionDetails.builder()
            .pspTransactionRef("MP241209.1404.B96583")
            .pspTransactionAmount(288000)
            .status(SUCCESS)
            .build(),
        MobileTransactionDetails.builder()
            .pspTransactionRef("MP241210.0817.B36568")
            .pspTransactionAmount(288000)
            .status(SUCCESS)
            .build(),
        MobileTransactionDetails.builder()
            .pspTransactionRef("MP241210.1028.D46037")
            .pspTransactionAmount(288000)
            .status(SUCCESS)
            .build(),
        MobileTransactionDetails.builder()
            .pspTransactionRef("MP241210.1147.A49685")
            .pspTransactionAmount(265000)
            .status(SUCCESS)
            .build(),
        MobileTransactionDetails.builder()
            .pspTransactionRef("MP241210.1241.C53158")
            .pspTransactionAmount(288000)
            .status(SUCCESS)
            .build(),
        MobileTransactionDetails.builder()
            .pspTransactionRef("MP241211.2027.A49333")
            .pspTransactionAmount(265000)
            .status(SUCCESS)
            .build(),
        MobileTransactionDetails.builder()
            .pspTransactionRef("MP241211.2315.C57348")
            .pspTransactionAmount(265000)
            .status(SUCCESS)
            .build(),
        MobileTransactionDetails.builder()
            .pspTransactionRef("MP241212.0655.D65919")
            .pspTransactionAmount(288000)
            .status(SUCCESS)
            .build(),
        MobileTransactionDetails.builder()
            .pspTransactionRef("MP241212.0959.D75969")
            .pspTransactionAmount(288000)
            .status(SUCCESS)
            .build(),
        MobileTransactionDetails.builder()
            .pspTransactionRef("MP241212.1733.C01770")
            .pspTransactionAmount(330000)
            .status(SUCCESS)
            .build(),
        MobileTransactionDetails.builder()
            .pspTransactionRef("MP241212.1804.A03686")
            .pspTransactionAmount(265000)
            .status(SUCCESS)
            .build(),
        MobileTransactionDetails.builder()
            .pspTransactionRef("MP241212.1810.C04098")
            .pspTransactionAmount(288000)
            .status(SUCCESS)
            .build(),
        MobileTransactionDetails.builder()
            .pspTransactionRef("MP241213.1107.A42802")
            .pspTransactionAmount(330000)
            .status(SUCCESS)
            .build(),
        MobileTransactionDetails.builder()
            .pspTransactionRef("MP241214.0858.A99067")
            .pspTransactionAmount(288000)
            .status(SUCCESS)
            .build(),
        MobileTransactionDetails.builder()
            .pspTransactionRef("MP241214.1114.D09555")
            .pspTransactionAmount(288000)
            .status(SUCCESS)
            .build(),
        MobileTransactionDetails.builder()
            .pspTransactionRef("MP241214.1337.D17845")
            .pspTransactionAmount(288000)
            .status(SUCCESS)
            .build(),
        MobileTransactionDetails.builder()
            .pspTransactionRef("MP241215.1137.D71706")
            .pspTransactionAmount(288000)
            .status(SUCCESS)
            .build());
  }

  @Test
  void xlsx_correctly_extracted() {

    var fakePendingSavedMpbs =
        excelTransactionDetails().stream()
            .map(
                t ->
                    (school.hei.haapi.model.mpbs.Mpbs)
                        school.hei.haapi.model.mpbs.Mpbs.builder()
                            .pspId(t.getPspTransactionRef())
                            .build())
            .toList();
    when(mockedMpbsRepository.findByPspIdIn(anyList())).thenReturn(List.of());
    when(mockedMpbsRepository.findAllByStatus(PENDING)).thenReturn(fakePendingSavedMpbs);

    assertDoesNotThrow(() -> subject.computeFromXls(getMockedFile("test-mpbs", ".xls")));

    ArgumentCaptor<List<MobileTransactionDetails>> argumentCaptor =
        ArgumentCaptor.forClass(List.class);
    verify(mobilePaymentService, times(1)).saveAll(argumentCaptor.capture());
    var captured = argumentCaptor.getAllValues().getFirst();
    captured.forEach(
        mobileTransactionDetails -> {
          mobileTransactionDetails.setId(null);
          // Todo: verify if transaction date match with the content
          mobileTransactionDetails.setPspDatetimeTransactionCreation(null);
          mobileTransactionDetails.setPspOwnDatetimeVerification(null);
        });
    assertEquals(excelTransactionDetails().size(), captured.size());
    assertTrue(excelTransactionDetails().containsAll(captured));
  }
}
