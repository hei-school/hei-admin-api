package school.hei.haapi.unit.validator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_ID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.rest.model.CreateFee;
import school.hei.haapi.endpoint.rest.model.CrupdateStudentFee;
import school.hei.haapi.endpoint.rest.security.AuthProvider;
import school.hei.haapi.endpoint.rest.validator.CreateFeeValidator;
import school.hei.haapi.endpoint.rest.validator.CrupdateStudentFeeValidator;
import school.hei.haapi.model.exception.BadRequestException;

public class FeeValidatorTest {
  CreateFeeValidator subject;
  CrupdateStudentFeeValidator crupdateFeeValidator;
  AuthProvider authProvider;

  @BeforeEach
  void setUp() {
    authProvider = mock(AuthProvider.class);
    subject = new CreateFeeValidator(authProvider);
    crupdateFeeValidator = new CrupdateStudentFeeValidator();
  }

  static CreateFee createFee() {
    return new CreateFee().totalAmount(1_000);
  }

  static CrupdateStudentFee crupdateStudentFee() {
    return new CrupdateStudentFee().studentId(STUDENT1_ID).totalAmount(5000);
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

  @Test
  void crupdate_fee_ok() {
    assertDoesNotThrow(() -> crupdateFeeValidator.accept(crupdateStudentFee()));
  }

  @Test
  void crupdate_fee_ko() {
    assertThrows(
        BadRequestException.class, () -> crupdateFeeValidator.accept(new CrupdateStudentFee()));
  }
}
