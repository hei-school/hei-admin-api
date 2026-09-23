package school.hei.haapi.unit.sms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaQuery;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.data.domain.PageRequest;
import school.hei.haapi.model.SmsContact;
import school.hei.haapi.model.SmsContactOwnerRole;
import school.hei.haapi.repository.dao.SmsContactDao;

class SmsContactDaoTest {
  private final EntityManager entityManagerMock =
      mock(EntityManager.class, Answers.RETURNS_DEEP_STUBS);
  private final SmsContactDao subject = new SmsContactDao(entityManagerMock);

  @Test
  void filters_by_contact_group_and_owner_role() {
    var expected = List.of(SmsContact.builder().id("c1").build());
    when(entityManagerMock.createQuery(any(CriteriaQuery.class)).getResultList())
        .thenReturn((List) expected);

    var result = subject.filterByCriteria("g1", SmsContactOwnerRole.STUDENT, PageRequest.of(0, 10));

    assertEquals(expected, result);
  }

  @Test
  void filters_by_contact_group_only() {
    var expected = List.of(SmsContact.builder().id("c1").build());
    when(entityManagerMock.createQuery(any(CriteriaQuery.class)).getResultList())
        .thenReturn((List) expected);

    var result = subject.filterByCriteria("g1", null, PageRequest.of(0, 10));

    assertEquals(expected, result);
  }

  @Test
  void filters_by_owner_role_only() {
    var expected = List.of(SmsContact.builder().id("c1").build());
    when(entityManagerMock.createQuery(any(CriteriaQuery.class)).getResultList())
        .thenReturn((List) expected);

    var result = subject.filterByCriteria(null, SmsContactOwnerRole.ADMIN, PageRequest.of(0, 10));

    assertEquals(expected, result);
  }

  @Test
  void works_without_any_filter() {
    var expected = List.<SmsContact>of();
    when(entityManagerMock.createQuery(any(CriteriaQuery.class)).getResultList())
        .thenReturn((List) expected);

    var result = subject.filterByCriteria(null, null, PageRequest.of(0, 10));

    assertEquals(expected, result);
  }
}
