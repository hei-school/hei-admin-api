package school.hei.haapi.service.event;

import java.io.File;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.event.model.RequestReceiptGeneration;
import school.hei.haapi.service.ReceiptGenerationService;

@Service
@AllArgsConstructor
@Slf4j
public class SendReceiptGenerationService implements Consumer<RequestReceiptGeneration> {
  private final ReceiptGenerationService receiptGenerationService;

  @SneakyThrows
  @Override
  public void accept(RequestReceiptGeneration requestReceiptGeneration) {
    File feeReceiptGenerated =
        receiptGenerationService.generatePaidFeeReceipt(requestReceiptGeneration.getPayments());
    receiptGenerationService.saveReceipt(
        feeReceiptGenerated, requestReceiptGeneration.getPayments());
  }
}
