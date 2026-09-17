package school.hei.haapi.unit.sms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.rest.mapper.SmsContactMapper;
import school.hei.haapi.model.User;

class SmsContactMapperTest {
  private final SmsContactMapper subject = new SmsContactMapper();

  @Test
  void every_domain_owner_role_maps_to_its_rest_counterpart_by_name() {
    for (var domainRole : school.hei.haapi.model.SmsContactOwnerRole.values()) {
      var rest = subject.toRest(domainRole);
      assertEquals(domainRole.name(), rest.name());
      assertEquals(domainRole, subject.toDomain(rest));
    }
  }

  @Test
  void null_owner_role_is_null_safe_both_ways() {
    assertNull(subject.toRest((school.hei.haapi.model.SmsContactOwnerRole) null));
    assertNull(subject.toDomain(null));
  }

  @Test
  void contact_name_joins_first_and_last_name() {
    var user = User.builder().firstName("Antenaina").lastName("Jaonina").build();
    assertEquals("Antenaina Jaonina", SmsContactMapper.toContactName(user));
  }

  @Test
  void contact_name_tolerates_missing_first_or_last_name() {
    var user = User.builder().firstName(null).lastName("Jaonina").build();
    assertEquals(" Jaonina", SmsContactMapper.toContactName(user));
  }
}
