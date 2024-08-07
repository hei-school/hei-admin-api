package school.hei.haapi.unit.validator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.rest.model.CreateFee;
import school.hei.haapi.endpoint.rest.validator.CreateFeeValidator;
import school.hei.haapi.model.exception.BadRequestException;

public class FeeValidatorTest {
  CreateFeeValidator subject;

  static CreateFee createFee() {
    return new CreateFee().totalAmount(1_000);
  }

  @BeforeEach
  void setUp() {
    subject = new CreateFeeValidator();
  }

  @Test
  void create_fee_ok() {
    assertDoesNotThrow(() -> subject.accept(createFee()));
  }

  @Test
  void create_fee_ko() {
    assertThrows(BadRequestException.class, () -> subject.accept(new CreateFee()));
  }
}
