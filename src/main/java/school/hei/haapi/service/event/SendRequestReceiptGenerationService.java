package school.hei.haapi.service.event;

import java.io.File;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.event.model.SendRequestReceiptGeneration;
import school.hei.haapi.service.ReceiptGenerationService;

@Service
@AllArgsConstructor
@Slf4j
public class SendRequestReceiptGenerationService implements Consumer<SendRequestReceiptGeneration> {
  private final ReceiptGenerationService receiptGenerationService;

  @Override
  public void accept(SendRequestReceiptGeneration sendRequestReceiptGeneration) {
    try {
      File feeReceiptGenerated =
          receiptGenerationService.generatePaidFeeReceipt(
              sendRequestReceiptGeneration.getPayment());
      receiptGenerationService.saveReceipt(
          feeReceiptGenerated, sendRequestReceiptGeneration.getPayment());
      log.info("Payment: {} are generated", sendRequestReceiptGeneration.getPayment());
    } catch (Exception e) {
      log.error(
          "Receipt generation failed for {} with error : {}",
          sendRequestReceiptGeneration.getPayment(),
          e.getMessage());
    }
  }
}
