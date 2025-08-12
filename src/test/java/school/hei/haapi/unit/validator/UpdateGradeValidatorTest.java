package school.hei.haapi.unit.validator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.rest.model.UpdateGrade;
import school.hei.haapi.endpoint.rest.validator.UpdateGradeValidator;
import school.hei.haapi.model.exception.BadRequestException;

class UpdateGradeValidatorTest {
  public final UpdateGradeValidator subject = new UpdateGradeValidator(mock());

  @Test
  void valid_grade_update_ok() {
    assertDoesNotThrow(() -> subject.accept(new UpdateGrade()));
  }

  @Test
  void invalid_grade_update_ko() {
    UpdateGrade updateGrade = new UpdateGrade();
    BadRequestException badRequestException =
        assertThrows(BadRequestException.class, () -> subject.accept(updateGrade));
    assertEquals(
        "Grade modification must be followed by comment about the change",
        badRequestException.getMessage());
  }
}
