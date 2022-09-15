package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Place;
import school.hei.haapi.repository.PlaceRepository;

import javax.transaction.Transactional;
import java.util.List;

@Service
@AllArgsConstructor
public class PlaceService {
    private final PlaceRepository placeRepository;

    public List<Place> getAll(){
        return placeRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    public Place getById(String id){
        return placeRepository.getById(id);
    }

    @Transactional
    public List<Place> createOrUpdate(List<Place> places){
        return placeRepository.saveAll(places);
    }

    public Place getPlaceById(String placeId) {
        return placeRepository.getById(placeId);
    }
}
