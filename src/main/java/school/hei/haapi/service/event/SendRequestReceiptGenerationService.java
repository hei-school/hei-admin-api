package school.hei.haapi.service.event;

import java.io.File;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.event.model.SendRequestReceiptGeneration;
import school.hei.haapi.service.ReceiptGenerationService;

@Service
@AllArgsConstructor
@Slf4j
public class SendRequestReceiptGenerationService implements Consumer<SendRequestReceiptGeneration> {
  private final ReceiptGenerationService receiptGenerationService;

  @SneakyThrows
  @Override
  public void accept(SendRequestReceiptGeneration sendRequestReceiptGeneration) {
    File feeReceiptGenerated =
        receiptGenerationService.generatePaidFeeReceipt(sendRequestReceiptGeneration.getPayments());
    receiptGenerationService.saveReceipt(
        feeReceiptGenerated, sendRequestReceiptGeneration.getPayments());
  }
}
