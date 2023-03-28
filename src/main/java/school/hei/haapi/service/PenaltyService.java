package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Penalty;
import school.hei.haapi.repository.PenaltyRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class PenaltyService {
    private final PenaltyRepository penaltyRepository;

    public Penalty getAll()
    {
        List<Penalty> result = penaltyRepository.findAll();

        return result.get(0);
    }

}
