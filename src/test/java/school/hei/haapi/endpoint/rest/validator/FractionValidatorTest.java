package school.hei.haapi.endpoint.rest.validator;

import static java.lang.Integer.MAX_VALUE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.rest.model.Fraction;
import school.hei.haapi.model.exception.BadRequestException;

class FractionValidatorTest {

  private final FractionValidator fractionValidator = new FractionValidator();

  @Test
  void accept_WithValidFraction_ShouldNotThrowException() {
    assertDoesNotThrow(() -> fractionValidator.accept(aFraction(1, 2)));
  }

  @Test
  void accept_WithNullFraction_ShouldThrowBadRequestException() {
    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> fractionValidator.accept(null));
    assertEquals("Provided fraction is null", exception.getMessage());
  }

  @Test
  void accept_WithNullNumerator_ShouldThrowBadRequestException() {
    var fraction = aFraction(null, 1);

    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> fractionValidator.accept(fraction));
    assertEquals("Components of the fraction cannot be null", exception.getMessage());
  }

  @Test
  void accept_WithNullDenominator_ShouldThrowBadRequestException() {
    var fraction = aFraction(1, null);

    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> fractionValidator.accept(fraction));
    assertEquals("Components of the fraction cannot be null", exception.getMessage());
  }

  @Test
  void accept_WithBothNullComponents_ShouldThrowBadRequestException() {
    var fraction = aFraction(null, null);

    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> fractionValidator.accept(fraction));
    assertEquals("Components of the fraction cannot be null", exception.getMessage());
  }

  @Test
  void accept_WithZeroNumerator_ShouldThrowBadRequestException() {
    var fraction = aFraction(0, 2);

    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> fractionValidator.accept(fraction));
    assertEquals(
        "Components of the fraction cannot be less or equal than 0", exception.getMessage());
  }

  @Test
  void accept_WithNegativeNumerator_ShouldThrowBadRequestException() {
    var fraction = aFraction(-5, 2);

    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> fractionValidator.accept(fraction));
    assertEquals(
        "Components of the fraction cannot be less or equal than 0", exception.getMessage());
  }

  @Test
  void accept_WithZeroDenominator_ShouldThrowBadRequestException() {
    var fraction = aFraction(1, 0);

    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> fractionValidator.accept(fraction));
    assertEquals(
        "Components of the fraction cannot be less or equal than 0", exception.getMessage());
  }

  @Test
  void accept_WithNegativeDenominator_ShouldThrowBadRequestException() {
    var fraction = aFraction(2, -3);

    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> fractionValidator.accept(fraction));
    assertEquals(
        "Components of the fraction cannot be less or equal than 0", exception.getMessage());
  }

  @Test
  void accept_WithBothNegativeComponents_ShouldThrowBadRequestException() {
    var fraction = aFraction(-2, -3);

    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> fractionValidator.accept(fraction));
    assertEquals(
        "Components of the fraction cannot be less or equal than 0", exception.getMessage());
  }

  @Test
  void accept_WithBothZeroComponents_ShouldThrowBadRequestException() {
    var fraction = aFraction(0, 0);

    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> fractionValidator.accept(fraction));
    assertEquals(
        "Components of the fraction cannot be less or equal than 0", exception.getMessage());
  }

  @Test
  void accept_WithLargePositiveValues_ShouldNotThrowException() {
    var fraction = aFraction(MAX_VALUE, MAX_VALUE);

    assertDoesNotThrow(() -> fractionValidator.accept(fraction));
  }

  @Test
  void accept_WithMinimumPositiveValues_ShouldNotThrowException() {
    var fraction = aFraction(1, 1);

    assertDoesNotThrow(() -> fractionValidator.accept(fraction));
  }

  private Fraction aFraction(Integer numerator, Integer denominator) {
    return new Fraction().numerator(numerator).denominator(denominator);
  }
}
