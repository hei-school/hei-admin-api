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
import school.hei.haapi.model.SmsCampaign;
import school.hei.haapi.model.SmsCampaignStatus;
import school.hei.haapi.repository.dao.SmsCampaignDao;

class SmsCampaignDaoTest {
  private final EntityManager entityManagerMock =
      mock(EntityManager.class, Answers.RETURNS_DEEP_STUBS);
  private final SmsCampaignDao subject = new SmsCampaignDao(entityManagerMock);

  @Test
  void filters_by_status_when_one_is_given() {
    var expected = List.of(SmsCampaign.builder().id("campaign1").build());
    when(entityManagerMock.createQuery(any(CriteriaQuery.class)).getResultList())
        .thenReturn((List) expected);

    var result = subject.filterByCriteria(SmsCampaignStatus.DELIVERED, PageRequest.of(0, 10));

    assertEquals(expected, result);
  }

  @Test
  void works_without_any_status_filter() {
    var expected = List.<SmsCampaign>of();
    when(entityManagerMock.createQuery(any(CriteriaQuery.class)).getResultList())
        .thenReturn((List) expected);

    var result = subject.filterByCriteria(null, PageRequest.of(0, 10));

    assertEquals(expected, result);
  }
}
