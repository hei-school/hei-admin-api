package school.hei.haapi.unit.validator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static school.hei.haapi.endpoint.rest.model.FeeCategory.WORK_FEES;
import static school.hei.haapi.endpoint.rest.model.FeeTypeEnum.TUITION;
import static school.hei.haapi.integration.conf.ApiAssertions.assertThrowsDomainBadRequestException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.rest.model.FeeCategory;
import school.hei.haapi.endpoint.rest.model.FeeTemplateContent;
import school.hei.haapi.endpoint.rest.model.FeeTypeEnum;
import school.hei.haapi.endpoint.rest.model.V2CrupdateFeeTemplate;
import school.hei.haapi.endpoint.rest.validator.FeeTemplateValidator;

class FeeTemplateValidatorTest {
  private final FeeTemplateValidator subject = new FeeTemplateValidator();

  private static V2CrupdateFeeTemplate aFeeTemplate(
      String id, String label, FeeTypeEnum type, FeeCategory category) {
    return new V2CrupdateFeeTemplate().id(id).label(label).type(type).category(category);
  }

  private static V2CrupdateFeeTemplate validFeeTemplate() {
    return aFeeTemplate("template_id", "Tuition", TUITION, WORK_FEES);
  }

  private static FeeTemplateContent aContent(
      String id, String label, Integer amount, LocalDate dueDate) {
    return new FeeTemplateContent().id(id).label(label).amount(amount).dueDate(dueDate);
  }

  private static FeeTemplateContent validContent() {
    return aContent("content_id", "January", 5000, LocalDate.of(2026, 1, 31));
  }

  @Test
  void accept_valid_fee_templates_does_not_throw() {
    assertDoesNotThrow(() -> subject.accept(List.of(validFeeTemplate(), validFeeTemplate())));
  }

  @Test
  void accept_empty_fee_templates_does_not_throw() {
    assertDoesNotThrow(() -> subject.accept(List.of()));
  }

  @Test
  void accept_null_fee_templates_list_throws() {
    assertThrowsDomainBadRequestException(
        "Provided fee templates list is null",
        () -> subject.accept((List<V2CrupdateFeeTemplate>) null));
  }

  @Test
  void accept_null_fee_template_item_throws() {
    assertThrowsDomainBadRequestException(
        "Provided fee template is null",
        () -> subject.accept(Arrays.asList(validFeeTemplate(), null)));
  }

  @Test
  void accept_fee_template_with_null_id_throws() {
    var feeTemplate = aFeeTemplate(null, "Tuition", TUITION, WORK_FEES);
    assertThrowsDomainBadRequestException(
        "Fee template id is mandatory", () -> subject.accept(List.of(feeTemplate)));
  }

  @Test
  void accept_fee_template_with_null_label_throws() {
    var feeTemplate = aFeeTemplate("template_id", null, TUITION, WORK_FEES);
    assertThrowsDomainBadRequestException(
        "Fee template label is mandatory", () -> subject.accept(List.of(feeTemplate)));
  }

  @Test
  void accept_fee_template_with_null_type_throws() {
    var feeTemplate = aFeeTemplate("template_id", "Tuition", null, WORK_FEES);
    assertThrowsDomainBadRequestException(
        "Fee template type is mandatory", () -> subject.accept(List.of(feeTemplate)));
  }

  @Test
  void accept_fee_template_with_null_category_throws() {
    var feeTemplate = aFeeTemplate("template_id", "Tuition", TUITION, null);
    assertThrowsDomainBadRequestException(
        "Fee template category is mandatory", () -> subject.accept(List.of(feeTemplate)));
  }

  @Test
  void acceptContents_valid_contents_does_not_throw() {
    assertDoesNotThrow(() -> subject.acceptContents(List.of(validContent(), validContent())));
  }

  @Test
  void acceptContents_empty_contents_does_not_throw() {
    assertDoesNotThrow(() -> subject.acceptContents(List.of()));
  }

  @Test
  void acceptContents_null_contents_list_throws() {
    assertThrowsDomainBadRequestException(
        "Provided fee template contents list is null",
        () -> subject.acceptContents((List<FeeTemplateContent>) null));
  }

  @Test
  void acceptContents_null_content_item_throws() {
    assertThrowsDomainBadRequestException(
        "Provided fee template content is null",
        () -> subject.acceptContents(Arrays.asList(validContent(), null)));
  }

  @Test
  void acceptContents_content_with_null_id_throws() {
    var content = aContent(null, "January", 5000, LocalDate.of(2026, 1, 31));
    assertThrowsDomainBadRequestException(
        "Fee template content id is mandatory", () -> subject.acceptContents(List.of(content)));
  }

  @Test
  void acceptContents_content_with_null_label_throws() {
    var content = aContent("content_id", null, 5000, LocalDate.of(2026, 1, 31));
    assertThrowsDomainBadRequestException(
        "Fee template content label is mandatory", () -> subject.acceptContents(List.of(content)));
  }

  @Test
  void acceptContents_content_with_null_amount_throws() {
    var content = aContent("content_id", "January", null, LocalDate.of(2026, 1, 31));
    assertThrowsDomainBadRequestException(
        "Fee template content amount is mandatory", () -> subject.acceptContents(List.of(content)));
  }

  @Test
  void acceptContents_content_with_zero_amount_throws() {
    var content = aContent("content_id", "January", 0, LocalDate.of(2026, 1, 31));
    assertThrowsDomainBadRequestException(
        "Fee template content amount must be greater than 0",
        () -> subject.acceptContents(List.of(content)));
  }

  @Test
  void acceptContents_content_with_negative_amount_throws() {
    var content = aContent("content_id", "January", -100, LocalDate.of(2026, 1, 31));
    assertThrowsDomainBadRequestException(
        "Fee template content amount must be greater than 0",
        () -> subject.acceptContents(List.of(content)));
  }

  @Test
  void acceptContents_content_with_null_due_date_throws() {
    var content = aContent("content_id", "January", 5000, null);
    assertThrowsDomainBadRequestException(
        "Fee template content due date is mandatory",
        () -> subject.acceptContents(List.of(content)));
  }
}
