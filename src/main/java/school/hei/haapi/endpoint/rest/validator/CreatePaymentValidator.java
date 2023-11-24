package school.hei.haapi.endpoint.rest.validator;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalField;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CreatePayment;
import school.hei.haapi.model.exception.BadRequestException;

@Component
public class CreatePaymentValidator implements Consumer<CreatePayment> {
  @Override
  public void accept(CreatePayment createPayment) {
    Instant now = Instant.now();
    if (createPayment.getAmount() == null) {
      throw new BadRequestException("Amount is mandatory");
    }
    if (createPayment.getCreationDatetime() == null) {
      throw new BadRequestException("Creation datetime is mandatory");
    }
    if (createPayment.getCreationDatetime().isAfter(now)) {
      throw new BadRequestException(
          "Creation datetime is invalid" );
    }
  }
}
