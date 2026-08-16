package school.hei.haapi.unit.sequence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.model.PaymentNumberSequence;
import school.hei.haapi.service.PaymentNumberSequenceService;

@Testcontainers
@AutoConfigureMockMvc
public class PaymentSequenceTest extends FacadeITMockedThirdParties {
  @Autowired PaymentNumberSequenceService subject;

  @Test
  void generated_sequence_is_sequential() {
    var date = LocalDate.now();
    List<PaymentNumberSequence> result = new ArrayList<>();

    for (int i = 0; i < 10; i++) {
      result.add(subject.getNextSequence(LocalDate.now()));
    }

    for (int i = 0; i < 10; i++) {
      var expected = String.format("%s-%s-%04d", date.getYear(), date.getMonthValue(), i + 1);
      assertEquals(expected, result.get(i).getStringSequence());
    }
  }
}
