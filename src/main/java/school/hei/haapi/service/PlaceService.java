package school.hei.haapi.service;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Place;
import school.hei.haapi.repository.PlaceRepository;

@Service
@AllArgsConstructor
public class PlaceService {

    private final PlaceRepository repository;

    public Place getById(String placeId) {
        return repository.getById(placeId);
    }

    public List<Place> getAll() {
        return repository.findAll();
    }

    public List<Place> saveAll(List<Place> places) {
        return repository.saveAll(places);
    }
}