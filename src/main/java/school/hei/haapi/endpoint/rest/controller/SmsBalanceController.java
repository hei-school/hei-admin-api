package school.hei.haapi.endpoint.rest.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.model.SmsBalance;
import school.hei.haapi.service.befiana.BefianaClient;

@RestController
@RequiredArgsConstructor
public class SmsBalanceController {
  private final BefianaClient befianaClient;

  @GetMapping("/sms-balance")
  public SmsBalance getSmsBalance() {
    return new SmsBalance().availableBalance(befianaClient.getBalance());
  }
}
