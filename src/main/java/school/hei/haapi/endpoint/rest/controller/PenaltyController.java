package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.model.Penalty;
import school.hei.haapi.service.PenaltyService;

import java.util.List;

@RestController
@AllArgsConstructor
public class PenaltyController {
    private PenaltyService penaltyService;

    @GetMapping("/delay_penalty")
    public List<Penalty> getAllPenalty(){
        return penaltyService.getAll();
    }

    @PutMapping("delay_penalty_change")
    public Penalty createOrUpdatePenalty(@RequestBody Penalty penalty)
    { return penaltyService.save(penalty);}
}
