package school.hei.haapi.service;

import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.PaymentNumberSequence;
import school.hei.haapi.repository.PaymentNumberSequenceRepository;

@Service
@RequiredArgsConstructor
public class PaymentNumberSequenceService {
  private final PaymentNumberSequenceRepository repository;

  public PaymentNumberSequence getNextSequence(LocalDate date) {
    String yearMonth = getYearMonth(date);
    Optional<PaymentNumberSequence> sequence =
        repository.findFirstByYearMonthOrderBySequenceNumberDesc(yearMonth);

    PaymentNumberSequence newSequence =
        PaymentNumberSequence.builder()
            .yearMonth(yearMonth)
            .sequenceNumber(sequence.map(s -> s.getSequenceNumber() + 1).orElse(1))
            .build();
    return repository.save(newSequence);
  }

  private static String getYearMonth(LocalDate date) {
    return date.getYear() + "-" + date.getMonthValue();
  }
}
