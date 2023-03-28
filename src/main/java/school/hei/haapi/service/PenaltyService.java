package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.repository.PenaltyRepository;

@Service
@AllArgsConstructor
public class PenaltyService {
    private final PenaltyRepository penaltyRepository;

}
