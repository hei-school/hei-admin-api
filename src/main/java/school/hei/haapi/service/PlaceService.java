package school.hei.haapi.service;

import javax.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Place;
import school.hei.haapi.repository.PlaceRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class PlaceService {

  private final PlaceRepository repository;

  public List<Place> getAll() {
    return repository.findAll();
  }

  public Place getById(String id) {
    return repository.getById(id);
  }

  @Transactional
  public Place save(Place place){
    return repository.save(place);
  }
}
