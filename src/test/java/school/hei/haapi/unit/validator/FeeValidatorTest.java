package school.hei.haapi.unit.validator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.rest.model.CreateFee;
import school.hei.haapi.endpoint.rest.security.AuthProvider;
import school.hei.haapi.endpoint.rest.validator.CreateFeeValidator;
import school.hei.haapi.model.exception.BadRequestException;

public class FeeValidatorTest {
  CreateFeeValidator subject;
  AuthProvider authProvider;

  static CreateFee createFee() {
    return new CreateFee().totalAmount(1_000);
  }

  @BeforeEach
  void setUp() {
    authProvider = mock(AuthProvider.class);
    subject = new CreateFeeValidator(authProvider);
  }

  @Test
  @Disabled
  void create_fee_ok() {
    assertDoesNotThrow(() -> subject.accept(createFee()));
  }

  @Test
  @Disabled
  void create_fee_ko() {
    assertThrows(BadRequestException.class, () -> subject.accept(new CreateFee()));
  }
}
