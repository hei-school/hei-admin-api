package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.model.Penalty;
import school.hei.haapi.service.PenaltyService;

import java.util.List;

@RestController
@AllArgsConstructor
public class PenaltyController {
    private PenaltyService service;

    @GetMapping("/delay_penalty")
    public List<Penalty> getAll(){
        return service.getAll();
    }

}
