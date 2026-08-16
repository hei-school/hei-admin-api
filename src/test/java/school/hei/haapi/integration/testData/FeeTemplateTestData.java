package school.hei.haapi.integration.testData;

import static java.util.UUID.randomUUID;
import static school.hei.haapi.endpoint.rest.model.FeeCategory.UNKNOWN;
import static school.hei.haapi.endpoint.rest.model.FeeFrequency.MONTHLY;
import static school.hei.haapi.endpoint.rest.model.FeeTypeEnum.TUITION;

import school.hei.haapi.endpoint.rest.model.FeeTypeEnum;
import school.hei.haapi.model.FeeTemplate;

public class FeeTemplateTestData {
  public static FeeTemplate aFeeTemplate(String name, int amount, int numberOfPayments) {
    return aFeeTemplate(name, amount, numberOfPayments, TUITION);
  }

  public static FeeTemplate aFeeTemplate(
      String name, int amount, int numberOfPayments, FeeTypeEnum type) {
    return FeeTemplate.builder()
        .id(randomUUID().toString())
        .name(name)
        .amount(amount)
        .numberOfPayments(numberOfPayments)
        .type(type)
        .category(UNKNOWN)
        .frequency(MONTHLY)
        .build();
  }
}
