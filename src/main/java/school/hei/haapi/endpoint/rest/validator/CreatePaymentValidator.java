package school.hei.haapi.endpoint.rest.validator;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CreatePayment;
import school.hei.haapi.model.exception.BadRequestException;

@Component
public class CreatePaymentValidator implements Consumer<CreatePayment> {
  @Override
  public void accept(CreatePayment createPayment) {
    LocalDateTime now = LocalDateTime.ofInstant(Instant.now(), ZoneId.of("UTC+3"));
    if (createPayment.getAmount() == null) {
      throw new BadRequestException("Amount is mandatory");
    }
    if (createPayment.getCreationDatetime() == null) {
      throw new BadRequestException("Creation datetime is mandatory");
    }
    if (createPayment.getCreationDatetime().isAfter(Instant.now())) {
      throw new BadRequestException(
          "Creation datetime must be before or equal to: " + now.getHour() + ":" + now.getMinute());
    }
  }
}
