package school.hei.haapi.unit.sms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import school.hei.haapi.model.SmsContact;
import school.hei.haapi.model.SmsContactOwnerRole;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.SmsContactRepository;
import school.hei.haapi.repository.dao.SmsContactDao;
import school.hei.haapi.service.sms.SmsContactService;

class SmsContactServiceTest {
  private final SmsContactRepository smsContactRepositoryMock = mock();
  private final SmsContactDao smsContactDaoMock = mock();

  private final SmsContactService subject =
      new SmsContactService(smsContactRepositoryMock, smsContactDaoMock);

  @Test
  void getByCriteria_delegates_to_the_dao() {
    var contact = SmsContact.builder().id("c1").build();
    when(smsContactDaoMock.filterByCriteria(eq("g1"), eq(SmsContactOwnerRole.STUDENT), any()))
        .thenReturn(List.of(contact));

    var result =
        subject.getByCriteria(
            "g1",
            SmsContactOwnerRole.STUDENT,
            org.springframework.data.domain.PageRequest.of(0, 10));

    assertEquals(List.of(contact), result);
  }

  @Test
  void getById_throws_not_found_for_a_missing_contact() {
    when(smsContactRepositoryMock.findById("missing")).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> subject.getById("missing"));
  }

  @Test
  void delete_soft_deletes_the_contact() {
    var contact = SmsContact.builder().id("c1").build();
    when(smsContactRepositoryMock.findById("c1")).thenReturn(Optional.of(contact));
    when(smsContactRepositoryMock.save(any())).thenAnswer(i -> i.getArgument(0));

    var deleted = subject.delete("c1");

    assertTrue(deleted.isDeleted());
  }
}
