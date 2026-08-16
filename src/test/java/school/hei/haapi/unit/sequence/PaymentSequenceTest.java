package school.hei.haapi.unit.sequence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.ArrayList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.model.PaymentNumberSequence;
import school.hei.haapi.service.PaymentNumberSequenceService;

public class PaymentSequenceTest extends FacadeITMockedThirdParties {
  private static final LocalDate OWN_MONTH = LocalDate.parse("2087-03-01");
  private static final String OWN_YEAR_MONTH = "2087-3";

  @Autowired PaymentNumberSequenceService subject;
  @Autowired JdbcTemplate jdbcTemplate;

  @AfterEach
  void tearDown() {
    jdbcTemplate.update(
        "DELETE FROM \"payment_number_sequence\" WHERE year_month = ?", OWN_YEAR_MONTH);
  }

  @Test
  void generated_sequence_is_sequential() {
    var drawn = new ArrayList<PaymentNumberSequence>();
    for (int i = 0; i < 10; i++) {
      drawn.add(subject.getNextSequence(OWN_MONTH));
    }
    for (int i = 0; i < 10; i++) {
      assertEquals("%s-%04d".formatted(OWN_YEAR_MONTH, i + 1), drawn.get(i).getStringSequence());
    }
  }
}
