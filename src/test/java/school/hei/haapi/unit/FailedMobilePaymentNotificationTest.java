package school.hei.haapi.unit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.javafaker.Faker;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.model.PaidFeeByMpbsFailedNotificationBody;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.Mpbs.Mpbs;
import school.hei.haapi.model.Payment;
import school.hei.haapi.model.User;
import school.hei.haapi.service.FailedMobilePaymentNotification;

class FailedMobilePaymentNotificationTest {
  private final EventProducer<PaidFeeByMpbsFailedNotificationBody> eventProducerMock = mock();
  private final FailedMobilePaymentNotification subject =
      new FailedMobilePaymentNotification(eventProducerMock);
  private final Faker faker = new Faker();

  @Test
  void dont_throw_if_contain_null_values() {
    assertDoesNotThrow(() -> subject.accept(List.of(new Mpbs())));
  }

  @Test
  void skip_mpbs_with_null_amount() {
    var student =
        User.builder()
            .id(faker.idNumber().toString())
            .firstName(faker.name().firstName())
            .lastName(faker.name().lastName())
            .build();
    var fee = Fee.builder().comment("comment").student(student).build();
    Mpbs badMpbs = mock();
    when(badMpbs.getFee()).thenReturn(fee);
    when(badMpbs.getAmount()).thenReturn(null);
    Mpbs goodMpbs = mock();
    when(goodMpbs.getFee()).thenReturn(fee);
    when(goodMpbs.getAmount()).thenReturn(10);
    when(goodMpbs.getStudent()).thenReturn(student);

    assertDoesNotThrow(() -> subject.accept(List.of(badMpbs, goodMpbs)));

    ArgumentCaptor<List<PaidFeeByMpbsFailedNotificationBody>> captor =
        ArgumentCaptor.forClass(List.class);
    verify(eventProducerMock, times(1)).accept(captor.capture());
    List<PaidFeeByMpbsFailedNotificationBody> notificationBodyCaptor = captor.getValue();
    assertEquals(1, notificationBodyCaptor.size());
    assertEquals(
        PaidFeeByMpbsFailedNotificationBody.from(Payment.builder().amount(10).fee(fee).build()),
        notificationBodyCaptor.getFirst());
  }
}
