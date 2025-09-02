package school.hei.haapi.endpoint.rest.validator;

import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Fraction;
import school.hei.haapi.model.exception.BadRequestException;

@Component
@AllArgsConstructor
public class FractionValidator implements Consumer<Fraction> {

  @Override
  public void accept(Fraction fraction) {
    if (fraction == null) {
      throw new BadRequestException("Provided fraction is null");
    }
    if (fraction.getNumerator() == null || fraction.getDenominator() == null) {
      throw new BadRequestException("Components of the fraction cannot be null");
    }
    if (fraction.getNumerator() <= 0 || fraction.getDenominator() <= 0) {
      throw new BadRequestException("Components of the fraction cannot be less or equal than 0");
    }
  }
}
