package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.repository.PlaceRepository;

@Service
@AllArgsConstructor
public class PlaceService {

    private final PlaceRepository repository;
}
