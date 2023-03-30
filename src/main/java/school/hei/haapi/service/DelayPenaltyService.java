package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.repository.DelayPenaltyRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class DelayPenaltyService {

    @Autowired
    private final DelayPenaltyRepository repository;

    public List<DelayPenalty> getDelayPenalty(){
        return repository.findFirstByOrderByCreationDatetimeDesc();
    }
}
