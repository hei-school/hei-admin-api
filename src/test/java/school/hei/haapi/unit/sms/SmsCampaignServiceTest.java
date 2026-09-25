package school.hei.haapi.unit.sms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.model.SmsCampaignDispatchRequested;
import school.hei.haapi.model.SmsCampaignStatus;
import school.hei.haapi.model.SmsRecipientSource;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.model.exception.SmsInsufficientBalanceException;
import school.hei.haapi.repository.SmsCampaignRepository;
import school.hei.haapi.repository.SmsContactRepository;
import school.hei.haapi.repository.SmsLogRepository;
import school.hei.haapi.repository.dao.SmsCampaignDao;
import school.hei.haapi.service.befiana.BefianaClient;
import school.hei.haapi.service.sms.ResolvedRecipient;
import school.hei.haapi.service.sms.SmsCampaignService;
import school.hei.haapi.service.sms.SmsCampaignService.CreateSmsCampaignCommand;
import school.hei.haapi.service.sms.SmsRecipientResolver;
import school.hei.haapi.service.sms.SmsSegmentCounter;

class SmsCampaignServiceTest {
  private final SmsCampaignRepository smsCampaignRepositoryMock = mock();
  private final SmsCampaignDao smsCampaignDaoMock = mock();
  private final SmsLogRepository smsLogRepositoryMock = mock();
  private final SmsContactRepository smsContactRepositoryMock = mock();
  private final SmsRecipientResolver smsRecipientResolverMock = mock();
  private final BefianaClient befianaClientMock = mock();
  private final EventProducer<SmsCampaignDispatchRequested> dispatchEventProducerMock = mock();

  private final SmsCampaignService subject =
      new SmsCampaignService(
          smsCampaignRepositoryMock,
          smsCampaignDaoMock,
          smsLogRepositoryMock,
          smsContactRepositoryMock,
          smsRecipientResolverMock,
          new SmsSegmentCounter(),
          befianaClientMock,
          dispatchEventProducerMock);

  private final User createdBy = User.builder().id("admin1").build();

  private static ResolvedRecipient manualNumber(String phoneNumber) {
    return new ResolvedRecipient(phoneNumber, SmsRecipientSource.MANUAL_NUMBER, null, null);
  }

  private SmsRecipientResolver.Resolved resolvedWith(List<ResolvedRecipient> recipients) {
    return new SmsRecipientResolver.Resolved(recipients, List.of(), List.of(), 0, null);
  }

  private void stubSaveAsIdentity() {
    when(smsCampaignRepositoryMock.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
  }

  private CreateSmsCampaignCommand command(String message, List<String> manualPhoneNumbers) {
    return new CreateSmsCampaignCommand(
        createdBy, message, null, null, manualPhoneNumbers, null, null);
  }

  @Test
  void balance_covering_everyone_creates_campaign_and_dispatches_immediately() {
    var numbers = List.of("321111111", "321111112", "321111113");
    var recipients = numbers.stream().map(SmsCampaignServiceTest::manualNumber).toList();
    when(smsRecipientResolverMock.resolve(null, null, numbers, null, null))
        .thenReturn(resolvedWith(recipients));
    when(befianaClientMock.getBalance()).thenReturn(10);
    stubSaveAsIdentity();

    var result = subject.createCampaign(command("Hello", numbers));

    assertEquals(3, result.campaign().getRecipientCount());
    assertEquals(0, result.campaign().getRecipientsRejectedForBalance());
    assertEquals(SmsCampaignStatus.CREATED, result.campaign().getStatus());
    verify(smsLogRepositoryMock, times(3)).save(any());
    verify(dispatchEventProducerMock, times(1))
        .accept(List.of(new SmsCampaignDispatchRequested(result.campaign().getId())));
  }

  @Test
  void balance_covering_nobody_rejects_without_persisting_anything() {
    var numbers = List.of("321111111", "321111112");
    var recipients = numbers.stream().map(SmsCampaignServiceTest::manualNumber).toList();
    when(smsRecipientResolverMock.resolve(null, null, numbers, null, null))
        .thenReturn(resolvedWith(recipients));
    when(befianaClientMock.getBalance()).thenReturn(0);
    var toCreate = command("Hello", numbers);

    var exception =
        assertThrows(SmsInsufficientBalanceException.class, () -> subject.createCampaign(toCreate));

    assertEquals(0, exception.getAvailableBalance());
    assertEquals(2, exception.getRecipientCount());
    assertEquals(0, exception.getMaxSendableRecipients());
    verify(smsCampaignRepositoryMock, never()).save(any());
    verify(smsLogRepositoryMock, never()).save(any());
    verify(dispatchEventProducerMock, never()).accept(any());
  }

  @Test
  void balance_covering_some_creates_a_partially_affordable_campaign() {
    var numbers = List.of("321111111", "321111112", "321111113");
    var recipients = numbers.stream().map(SmsCampaignServiceTest::manualNumber).toList();
    when(smsRecipientResolverMock.resolve(null, null, numbers, null, null))
        .thenReturn(resolvedWith(recipients));
    when(befianaClientMock.getBalance()).thenReturn(2);
    stubSaveAsIdentity();

    var result = subject.createCampaign(command("Hello", numbers));

    assertEquals(3, result.campaign().getRecipientCount());
    assertEquals(1, result.campaign().getRecipientsRejectedForBalance());
    verify(smsLogRepositoryMock, times(2)).save(any());
    verify(dispatchEventProducerMock, times(1)).accept(any());
  }

  @Test
  void a_personalized_recipients_own_message_drives_its_segment_cost_not_the_shared_one() {
    var longMessage = "a".repeat(200);
    var personalized =
        new ResolvedRecipient("321111111", SmsRecipientSource.IMPORTED_FILE, null, longMessage);
    when(smsRecipientResolverMock.resolve(null, null, null, null, null))
        .thenReturn(resolvedWith(List.of(personalized)));
    when(befianaClientMock.getBalance()).thenReturn(2);
    stubSaveAsIdentity();

    var result = subject.createCampaign(command("Hi", null));

    assertEquals(1, result.campaign().getRecipientCount());
    assertEquals(0, result.campaign().getRecipientsRejectedForBalance());
  }

  @Test
  void a_campaign_always_dispatches_immediately_there_is_no_scheduling() {
    var numbers = List.of("321111111");
    var recipients = numbers.stream().map(SmsCampaignServiceTest::manualNumber).toList();
    when(smsRecipientResolverMock.resolve(null, null, numbers, null, null))
        .thenReturn(resolvedWith(recipients));
    when(befianaClientMock.getBalance()).thenReturn(10);
    stubSaveAsIdentity();

    subject.createCampaign(command("Hello", numbers));

    verify(dispatchEventProducerMock, times(1)).accept(any());
  }

  @Test
  void getById_wraps_missing_campaign_in_not_found() {
    when(smsCampaignRepositoryMock.findById("missing")).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> subject.getById("missing"));
  }

  @Test
  void getByCriteria_delegates_to_the_dao() {
    var campaign = school.hei.haapi.model.SmsCampaign.builder().id("campaign1").build();
    when(smsCampaignDaoMock.filterByCriteria(eq(SmsCampaignStatus.DELIVERED), any()))
        .thenReturn(List.of(campaign));

    var result =
        subject.getByCriteria(
            SmsCampaignStatus.DELIVERED, org.springframework.data.domain.PageRequest.of(0, 10));

    assertEquals(List.of(campaign), result);
  }

  @Test
  void save_delegates_to_the_repository() {
    var campaign = school.hei.haapi.model.SmsCampaign.builder().id("campaign1").build();
    when(smsCampaignRepositoryMock.save(campaign)).thenReturn(campaign);

    assertEquals(campaign, subject.save(campaign));
  }
}
