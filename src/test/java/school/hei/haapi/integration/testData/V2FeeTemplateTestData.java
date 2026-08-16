package school.hei.haapi.integration.testData;

import static java.util.UUID.randomUUID;
import static school.hei.haapi.endpoint.rest.model.FeeCategory.WORK_FEES;
import static school.hei.haapi.endpoint.rest.model.FeeTypeEnum.TUITION;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import school.hei.haapi.model.V2FeeTemplate;
import school.hei.haapi.model.V2FeeTemplateContent;

public class V2FeeTemplateTestData {
  public static V2FeeTemplate aTwoMonthsTemplate() {
    var template =
        V2FeeTemplate.builder()
            .id(randomUUID().toString())
            .label("Tuition 2026")
            .type(TUITION)
            .category(WORK_FEES)
            .feeTemplateContents(new ArrayList<>())
            .build();

    template
        .getFeeTemplateContents()
        .addAll(
            List.of(
                aContent(template, "January", 5_000, LocalDate.parse("2026-01-31")),
                aContent(template, "February", 6_000, LocalDate.parse("2026-02-28"))));
    return template;
  }

  public static V2FeeTemplateContent aContent(
      V2FeeTemplate template, String label, int amount, LocalDate dueDate) {
    return V2FeeTemplateContent.builder()
        .id(randomUUID().toString())
        .feeTemplate(template)
        .label(label)
        .amount(BigInteger.valueOf(amount))
        .dueDate(dueDate)
        .build();
  }
}
