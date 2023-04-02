package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.model.Group;
import school.hei.haapi.repository.DelayPenaltyRepository;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class DelayPenaltyService {
    private final DelayPenaltyRepository repository;
    public List<DelayPenalty> getAll() {
        return repository.findAll();
    }

}
