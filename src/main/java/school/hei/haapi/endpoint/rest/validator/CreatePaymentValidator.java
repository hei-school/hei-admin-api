package school.hei.haapi.endpoint.rest.validator;

import static java.time.temporal.ChronoUnit.SECONDS;

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
    Instant now = Instant.now().truncatedTo(SECONDS);
    LocalDateTime localDateTimeNow = LocalDateTime.ofInstant(now, ZoneId.of("UTC"));
    if (createPayment.getAmount() == null) {
      throw new BadRequestException("Amount is mandatory");
    }
    if (createPayment.getCreationDatetime() == null) {
      throw new BadRequestException("Creation datetime is mandatory");
    }
    if (createPayment.getCreationDatetime().truncatedTo(SECONDS).isAfter(now)) {
      throw new BadRequestException(
          "Creation datetime must be before or equal to: "
              + localDateTimeNow.getHour()
              + ":"
              + localDateTimeNow.getMinute());
    }
  }
}
