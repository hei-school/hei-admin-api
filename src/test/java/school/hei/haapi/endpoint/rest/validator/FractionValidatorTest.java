package school.hei.haapi.endpoint.rest.validator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.rest.model.Fraction;
import school.hei.haapi.model.exception.BadRequestException;

class FractionValidatorTest {

  private FractionValidator fractionValidator;
  private Fraction validFraction;

  @BeforeEach
  void setUp() {
    fractionValidator = new FractionValidator();
    validFraction = new Fraction();
    validFraction.setNumerator(1);
    validFraction.setDenominator(2);
  }

  @Test
  void accept_WithValidFraction_ShouldNotThrowException() {
    assertDoesNotThrow(() -> fractionValidator.accept(validFraction));
  }

  @Test
  void accept_WithNullFraction_ShouldThrowBadRequestException() {
    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> fractionValidator.accept(null));
    assertEquals("Provided fraction is null", exception.getMessage());
  }

  @Test
  void accept_WithNullNumerator_ShouldThrowBadRequestException() {
    Fraction fraction = new Fraction();
    fraction.setNumerator(null);
    fraction.setDenominator(2);

    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> fractionValidator.accept(fraction));
    assertEquals("Components of the fraction cannot be null", exception.getMessage());
  }

  @Test
  void accept_WithNullDenominator_ShouldThrowBadRequestException() {
    Fraction fraction = new Fraction();
    fraction.setNumerator(1);
    fraction.setDenominator(null);

    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> fractionValidator.accept(fraction));
    assertEquals("Components of the fraction cannot be null", exception.getMessage());
  }

  @Test
  void accept_WithBothNullComponents_ShouldThrowBadRequestException() {
    Fraction fraction = new Fraction();
    fraction.setNumerator(null);
    fraction.setDenominator(null);

    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> fractionValidator.accept(fraction));
    assertEquals("Components of the fraction cannot be null", exception.getMessage());
  }

  @Test
  void accept_WithZeroNumerator_ShouldThrowBadRequestException() {
    Fraction fraction = new Fraction();
    fraction.setNumerator(0);
    fraction.setDenominator(2);

    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> fractionValidator.accept(fraction));
    assertEquals(
        "Components of the fraction cannot be less or equal than 0", exception.getMessage());
  }

  @Test
  void accept_WithNegativeNumerator_ShouldThrowBadRequestException() {
    Fraction fraction = new Fraction();
    fraction.setNumerator(-5);
    fraction.setDenominator(2);

    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> fractionValidator.accept(fraction));
    assertEquals(
        "Components of the fraction cannot be less or equal than 0", exception.getMessage());
  }

  @Test
  void accept_WithZeroDenominator_ShouldThrowBadRequestException() {
    Fraction fraction = new Fraction();
    fraction.setNumerator(1);
    fraction.setDenominator(0);

    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> fractionValidator.accept(fraction));
    assertEquals(
        "Components of the fraction cannot be less or equal than 0", exception.getMessage());
  }

  @Test
  void accept_WithNegativeDenominator_ShouldThrowBadRequestException() {
    Fraction fraction = new Fraction();
    fraction.setNumerator(1);
    fraction.setDenominator(-3);

    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> fractionValidator.accept(fraction));
    assertEquals(
        "Components of the fraction cannot be less or equal than 0", exception.getMessage());
  }

  @Test
  void accept_WithBothNegativeComponents_ShouldThrowBadRequestException() {
    Fraction fraction = new Fraction();
    fraction.setNumerator(-1);
    fraction.setDenominator(-2);

    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> fractionValidator.accept(fraction));
    assertEquals(
        "Components of the fraction cannot be less or equal than 0", exception.getMessage());
  }

  @Test
  void accept_WithBothZeroComponents_ShouldThrowBadRequestException() {
    Fraction fraction = new Fraction();
    fraction.setNumerator(0);
    fraction.setDenominator(0);

    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> fractionValidator.accept(fraction));
    assertEquals(
        "Components of the fraction cannot be less or equal than 0", exception.getMessage());
  }

  @Test
  void accept_WithLargePositiveValues_ShouldNotThrowException() {
    Fraction fraction = new Fraction();
    fraction.setNumerator(Integer.MAX_VALUE);
    fraction.setDenominator(Integer.MAX_VALUE);

    assertDoesNotThrow(() -> fractionValidator.accept(fraction));
  }

  @Test
  void accept_WithMinimumPositiveValues_ShouldNotThrowException() {
    Fraction fraction = new Fraction();
    fraction.setNumerator(1);
    fraction.setDenominator(1);

    assertDoesNotThrow(() -> fractionValidator.accept(fraction));
  }
}
