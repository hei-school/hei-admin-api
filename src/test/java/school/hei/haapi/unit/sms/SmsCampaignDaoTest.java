package school.hei.haapi.unit.sms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.data.domain.PageRequest;
import school.hei.haapi.model.SmsCampaign;
import school.hei.haapi.model.SmsCampaignStatus;
import school.hei.haapi.repository.dao.SmsCampaignDao;

/**
 * The actual filtering is delegated to the JPA criteria API (a real DB round-trip is out of scope
 * for a plain unit test here); this stubs each step of the EntityManager/CriteriaBuilder chain
 * explicitly rather than relying on Mockito's RETURNS_DEEP_STUBS to auto-wire it, because deep
 * stubs don't reliably resolve a stub declared with an argument matcher (e.g.
 * createQuery(any(CriteriaQuery.class))) back to the same TypedQuery mock once the code calls it
 * with the real (non-null) CriteriaQuery instance built at runtime.
 */
class SmsCampaignDaoTest {
  private final EntityManager entityManagerMock = mock();
  private final CriteriaBuilder criteriaBuilderMock =
      mock(CriteriaBuilder.class, Answers.RETURNS_DEEP_STUBS);
  private final CriteriaQuery<SmsCampaign> criteriaQueryMock =
      mock(CriteriaQuery.class, Answers.RETURNS_DEEP_STUBS);
  private final TypedQuery<SmsCampaign> typedQueryMock = mock();
  private final SmsCampaignDao subject = new SmsCampaignDao(entityManagerMock);

  @BeforeEach
  void wireUpTheCriteriaChain() {
    when(entityManagerMock.getCriteriaBuilder()).thenReturn(criteriaBuilderMock);
    when(criteriaBuilderMock.createQuery(SmsCampaign.class)).thenReturn(criteriaQueryMock);
    when(entityManagerMock.createQuery(criteriaQueryMock)).thenReturn(typedQueryMock);
    when(typedQueryMock.setFirstResult(anyInt())).thenReturn(typedQueryMock);
    when(typedQueryMock.setMaxResults(anyInt())).thenReturn(typedQueryMock);
  }

  @Test
  void filters_by_status_when_one_is_given() {
    var expected = List.of(SmsCampaign.builder().id("campaign1").build());
    when(typedQueryMock.getResultList()).thenReturn(expected);

    var result = subject.filterByCriteria(SmsCampaignStatus.DELIVERED, PageRequest.of(0, 10));

    assertEquals(expected, result);
  }

  @Test
  void works_without_any_status_filter() {
    var expected = List.<SmsCampaign>of();
    when(typedQueryMock.getResultList()).thenReturn(expected);

    var result = subject.filterByCriteria(null, PageRequest.of(0, 10));

    assertEquals(expected, result);
  }
}
