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

    public List<Penalty> getAll() {
        return penaltyRepository.findAll();
    }

    public Penalty saveOrUpdate(Penalty penalty) { return penaltyRepository.save(penalty); }
}
