package school.hei.haapi.endpoint.rest.validator;

import static java.lang.Integer.MAX_VALUE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsDomainBadRequestException;

import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.rest.model.Fraction;

class FractionValidatorTest {
  private final FractionValidator fractionValidator = new FractionValidator();

  @Test
  void accept_WithValidFraction_ShouldNotThrowException() {
    assertDoesNotThrow(() -> fractionValidator.accept(aFraction(1, 2)));
  }

  @Test
  void accept_WithNullFraction_ShouldThrowBadRequestException() {
    assertThrowsDomainBadRequestException(
        "Provided fraction is null", () -> fractionValidator.accept(null));
  }

  @Test
  void accept_WithNullNumerator_ShouldThrowBadRequestException() {
    var fraction = aFraction(null, 1);
    assertThrowsDomainBadRequestException(
        "Components of the fraction cannot be null", () -> fractionValidator.accept(fraction));
  }

  @Test
  void accept_WithNullDenominator_ShouldThrowBadRequestException() {
    var fraction = aFraction(1, null);
    assertThrowsDomainBadRequestException(
        "Components of the fraction cannot be null", () -> fractionValidator.accept(fraction));
  }

  @Test
  void accept_WithBothNullComponents_ShouldThrowBadRequestException() {
    var fraction = aFraction(null, null);
    assertThrowsDomainBadRequestException(
        "Components of the fraction cannot be null", () -> fractionValidator.accept(fraction));
  }

  @Test
  void accept_WithZeroNumerator_ShouldThrowBadRequestException() {
    var fraction = aFraction(0, 2);
    assertThrowsDomainBadRequestException(
        "Components of the fraction cannot be less or equal than 0",
        () -> fractionValidator.accept(fraction));
  }

  @Test
  void accept_WithNegativeNumerator_ShouldThrowBadRequestException() {
    var fraction = aFraction(-5, 2);
    assertThrowsDomainBadRequestException(
        "Components of the fraction cannot be less or equal than 0",
        () -> fractionValidator.accept(fraction));
  }

  @Test
  void accept_WithZeroDenominator_ShouldThrowBadRequestException() {
    var fraction = aFraction(1, 0);
    assertThrowsDomainBadRequestException(
        "Components of the fraction cannot be less or equal than 0",
        () -> fractionValidator.accept(fraction));
  }

  @Test
  void accept_WithNegativeDenominator_ShouldThrowBadRequestException() {
    var fraction = aFraction(2, -3);
    assertThrowsDomainBadRequestException(
        "Components of the fraction cannot be less or equal than 0",
        () -> fractionValidator.accept(fraction));
  }

  @Test
  void accept_WithBothNegativeComponents_ShouldThrowBadRequestException() {
    var fraction = aFraction(-2, -3);
    assertThrowsDomainBadRequestException(
        "Components of the fraction cannot be less or equal than 0",
        () -> fractionValidator.accept(fraction));
  }

  @Test
  void accept_WithBothZeroComponents_ShouldThrowBadRequestException() {
    var fraction = aFraction(0, 0);
    assertThrowsDomainBadRequestException(
        "Components of the fraction cannot be less or equal than 0",
        () -> fractionValidator.accept(fraction));
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
