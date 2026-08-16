package school.hei.haapi.unit.validator;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static school.hei.haapi.model.User.Role.MANAGER;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import school.hei.haapi.endpoint.rest.model.CreateFee;
import school.hei.haapi.endpoint.rest.model.CrupdateStudentFee;
import school.hei.haapi.endpoint.rest.security.AuthProvider;
import school.hei.haapi.endpoint.rest.security.model.Principal;
import school.hei.haapi.endpoint.rest.validator.CreateFeeValidator;
import school.hei.haapi.endpoint.rest.validator.CrupdateStudentFeeValidator;
import school.hei.haapi.model.User;
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

    // AuthProvider.getPrincipal() is static and reads the SecurityContextHolder, so mocking the
    // injected instance changes nothing: the caller has to be put in the context itself.
    var principal = new Principal(User.builder().role(MANAGER).build(), "a-token");
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, List.of()));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  static CreateFee createFee() {
    return new CreateFee().totalAmount(1_000);
  }

  static CrupdateStudentFee crupdateStudentFee() {
    return new CrupdateStudentFee().studentId(randomUUID().toString()).totalAmount(5000);
  }

  @Test
  void create_fee_ok() {
    assertDoesNotThrow(() -> subject.accept(createFee()));
  }

  @Test
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
