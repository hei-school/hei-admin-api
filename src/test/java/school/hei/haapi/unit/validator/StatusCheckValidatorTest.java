package school.hei.haapi.unit.validator;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static school.hei.haapi.endpoint.rest.model.StatusCheckResult.ENROLLED;
import static school.hei.haapi.endpoint.rest.model.StatusCheckResult.WITHDRAWN;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.rest.model.CreateStatusCheck;
import school.hei.haapi.endpoint.rest.model.UpdateStatusCheck;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.validator.CreateStatusCheckValidator;
import school.hei.haapi.model.validator.UpdateStatusCheckValidator;

class StatusCheckValidatorTest {
  CreateStatusCheckValidator createValidator;
  UpdateStatusCheckValidator updateValidator;

  @BeforeEach
  void setUp() {
    createValidator = new CreateStatusCheckValidator();
    updateValidator = new UpdateStatusCheckValidator();
  }

  @Test
  void create_null_object_should_throw() {
    assertThrows(BadRequestException.class, () -> createValidator.accept(null));
  }

  @Test
  void create_missing_fields_should_throw() {
    var check = new CreateStatusCheck();
    assertThrows(BadRequestException.class, () -> createValidator.accept(check));
  }

  @Test
  void create_all_fields_valid_should_pass() {
    var check =
        new CreateStatusCheck()
            .description("New status")
            .concernedStudentId("student1")
            .requestingUserId("user1");
    createValidator.accept(check);
  }

  @Test
  void update_null_object_should_throw() {
    assertThrows(BadRequestException.class, () -> updateValidator.accept(null));
  }

  @Test
  void update_blank_description_should_throw() {
    var check = new UpdateStatusCheck().description("");
    assertThrows(BadRequestException.class, () -> updateValidator.accept(check));
  }

  @Test
  void update_valid_description_only_should_pass() {
    var check = new UpdateStatusCheck().description("Updated description");
    updateValidator.accept(check);
  }

  @Test
  void update_valid_result_only_should_pass() {
    var check = new UpdateStatusCheck().result(ENROLLED);
    updateValidator.accept(check);
  }

  @Test
  void update_valid_description_and_result_should_pass() {
    var check = new UpdateStatusCheck().description("Everything is fine").result(WITHDRAWN);
    updateValidator.accept(check);
  }
}
