package school.hei.haapi.model.exception;

import lombok.Getter;

@Getter
public class SmsInsufficientBalanceException extends ApiException {
  private final int availableBalance;
  private final int recipientCount;
  private final int maxSendableRecipients;

  public SmsInsufficientBalanceException(
      String message, int availableBalance, int recipientCount, int maxSendableRecipients) {
    super(ExceptionType.CLIENT_EXCEPTION, message);
    this.availableBalance = availableBalance;
    this.recipientCount = recipientCount;
    this.maxSendableRecipients = maxSendableRecipients;
  }
}
