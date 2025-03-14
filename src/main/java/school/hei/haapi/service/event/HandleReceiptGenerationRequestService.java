package school.hei.haapi.service.event;

import static java.util.stream.Collectors.toUnmodifiableList;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.model.HandleReceiptGenerationRequest;
import school.hei.haapi.endpoint.event.model.SendRequestReceiptGeneration;
import school.hei.haapi.mail.Mailer;
import school.hei.haapi.model.dto.PaymentDto;
import school.hei.haapi.service.PaymentService;

@Service
@AllArgsConstructor
@Slf4j
public class HandleReceiptGenerationRequestService
    implements Consumer<HandleReceiptGenerationRequest> {
  private final PaymentService paymentService;
  private final Mailer mailer;
  private final EventProducer<SendRequestReceiptGeneration> eventProducer;

  @Override
  public void accept(HandleReceiptGenerationRequest handleReceiptGenerationRequest) {
    List<PaymentDto> updatedPayments = new ArrayList<>();
    List<PaymentDto> failedUpdatePayments = new ArrayList<>();

    for (PaymentDto payment : handleReceiptGenerationRequest.getPayments()) {
      try {
        updatedPayments.add(PaymentDto.from(paymentService.updateSequence(payment)));
      } catch (Exception e) {
        failedUpdatePayments.add(payment);
      }
    }

    // TODO: Send Email to the notifyEmail on the failed sequence generations.
    eventProducer.accept(
        updatedPayments.stream()
            .map(
                payment ->
                    SendRequestReceiptGeneration.builder()
                        .payment(payment)
                        .startRequest(Instant.now())
                        .build())
            .collect(toUnmodifiableList()));
    log.error("Failed to generate sequence for following payments: {}", failedUpdatePayments);
  }
}
