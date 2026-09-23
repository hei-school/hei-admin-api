package school.hei.haapi.service.sms;

import school.hei.haapi.model.SmsRecipientSource;

public record ResolvedRecipient(
    String phoneNumber, SmsRecipientSource source, String contactId, String personalizedMessage) {

  public boolean isPersonalized() {
    return personalizedMessage != null;
  }
}
