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
    String datePart = getDatePart(date);
    Optional<PaymentNumberSequence> sequence =
        repository.findFirstByDatePartOrderBySequenceNumberDesc(datePart);
    if (sequence.isEmpty()) {
      PaymentNumberSequence newSequence =
          PaymentNumberSequence.builder().datePart(datePart).sequenceNumber(1).build();
      return repository.save(newSequence);
    } else {
      Integer nextSequenceNumber = sequence.get().getSequenceNumber() + 1;
      PaymentNumberSequence newSequence =
          PaymentNumberSequence.builder()
              .datePart(datePart)
              .sequenceNumber(nextSequenceNumber)
              .build();
      return repository.save(newSequence);
    }
  }

  private String getDatePart(LocalDate date) {
    return date.getYear() + "-" + date.getMonthValue();
  }
}
