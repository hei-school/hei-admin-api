package school.hei.haapi.service;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.integration.conf.TestUtils.asserThrowsDomainNotFoundException;

import com.github.javafaker.Faker;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import school.hei.haapi.endpoint.rest.model.MpbsStatus;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.model.MobileTransactionDetails;
import school.hei.haapi.model.mpbs.Mpbs;

class MobilePaymentServiceTest extends FacadeITMockedThirdParties {
  private static final Faker faker = new Faker();
  private static final String MPBS_REF_GENERATION_PATTERN = "MP[0-9]{6}\\.[0-9]{4}\\.[A-Z0-9]{6}";

  @Autowired MobilePaymentService mobilePaymentService;

  private static List<MobileTransactionDetails> toSaveMobileTransactionDetails() {
    return IntStream.range(0, 10)
        .mapToObj(
            ignored ->
                MobileTransactionDetails.builder()
                    .id(randomUUID().toString())
                    .pspTransactionRef(faker.regexify(MPBS_REF_GENERATION_PATTERN))
                    .pspTransactionAmount(288000)
                    .status(faker.options().option(MpbsStatus.class))
                    .build())
        .toList();
  }

  @Test
  void save_all_mobile_transaction_details_ok() {
    var toSaveMobileTransactionDetails = toSaveMobileTransactionDetails();

    var savedMobileTransaction = mobilePaymentService.saveAll(toSaveMobileTransactionDetails);
    assertTrue(savedMobileTransaction.containsAll(toSaveMobileTransactionDetails));
  }

  @Test
  void save_already_saved_mobile_transaction_details_ok() {
    var toSaveMobileTransactionDetails = toSaveMobileTransactionDetails();

    var savedMobileTransaction = mobilePaymentService.saveAll(toSaveMobileTransactionDetails);
    assertTrue(savedMobileTransaction.containsAll(toSaveMobileTransactionDetails));
    assertTrue(
        mobilePaymentService
            .saveAll(savedMobileTransaction.stream().peek(e -> e.setId(null)).toList())
            .isEmpty());
  }

  @Test
  void get_transactions_by_mpbs_ok() {
    var toSaveMobileTransactionDetails = toSaveMobileTransactionDetails();
    var toSaveMobileTransactionMpbs = mpbsFromTransactions(toSaveMobileTransactionDetails);

    mobilePaymentService.saveAll(toSaveMobileTransactionDetails);

    assertTrue(
        mobilePaymentService
            .findAllTransactionByMpbs(toSaveMobileTransactionMpbs)
            .containsAll(toSaveMobileTransactionDetails));
  }

  @Test
  void find_transaction_by_ref_ok() {
    var toSaveMobileTransactionDetails = toSaveMobileTransactionDetails();
    var expected = toSaveMobileTransactionDetails.get(faker.number().numberBetween(0, 10));

    mobilePaymentService.saveAll(toSaveMobileTransactionDetails);

    var actual = mobilePaymentService.getTransactionByRef(expected.getPspTransactionRef());
    assertEquals(actual, expected);
  }

  @Test
  void find_transaction_by_mpbs() {
    var toSaveMobileTransactionDetails = toSaveMobileTransactionDetails();
    var expected = toSaveMobileTransactionDetails.get(faker.number().numberBetween(0, 10));

    mobilePaymentService.saveAll(toSaveMobileTransactionDetails);

    var actual = mobilePaymentService.getTransactionByRef(expected.getPspTransactionRef());
    assertEquals(actual, expected);
  }

  @Test
  void find_transaction_by_ref_not_found_ok() {
    var toSaveMobileTransactionDetails = toSaveMobileTransactionDetails();
    var randomTransactionRef = faker.regexify(MPBS_REF_GENERATION_PATTERN);

    mobilePaymentService.saveAll(toSaveMobileTransactionDetails);

    asserThrowsDomainNotFoundException(
        "Mobile transaction with ref " + randomTransactionRef + " not found",
        () -> mobilePaymentService.getTransactionByRef(randomTransactionRef));
  }

  private List<Mpbs> mpbsFromTransactions(List<MobileTransactionDetails> mobileTransactionDetails) {
    return mobileTransactionDetails.stream()
        .map(
            transaction ->
                (Mpbs)
                    Mpbs.builder()
                        .pspId(transaction.getPspTransactionRef())
                        .amount(transaction.getPspTransactionAmount())
                        .build())
        .toList();
  }
}
