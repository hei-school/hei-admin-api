package school.hei.haapi.model.validator;

import org.springframework.stereotype.Component;
import school.hei.haapi.model.DelayPenaltyHistory;
import school.hei.haapi.model.exception.BadRequestException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component
public class DelayPenaltyHistoryValidator implements Consumer<DelayPenaltyHistory> {

  public void accept(List<DelayPenaltyHistory> DelayPenaltyHistories) {
    DelayPenaltyHistories.forEach(this::accept);
  }

  @Override
  public void accept(DelayPenaltyHistory DelayPenaltyHistory) {
    Set<String> violationMessages = new HashSet<>();
    if (DelayPenaltyHistory.getTimeFrequency() < 0) {
      violationMessages.add("Time Frequency must be positive");
    }
    if (DelayPenaltyHistory.getInterestPercent() < 0) {
      violationMessages.add("Interest Percent must be positive");
    }
    if (DelayPenaltyHistory.getStartDate() == null) {
      violationMessages.add("Start date is mandatory");
    }
    if (DelayPenaltyHistory.getEndDate() != null) {
      if (DelayPenaltyHistory.getEndDate().isBefore(DelayPenaltyHistory.getStartDate())){
        violationMessages.add("Start date must be before end date");
      }
    }
    if (!violationMessages.isEmpty()) {
      String formattedViolationMessages = violationMessages.stream()
          .map(String::toString)
          .collect(Collectors.joining(". "));
      throw new BadRequestException(formattedViolationMessages);
    }
  }
}
