package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Place;
import school.hei.haapi.model.validator.PlaceValidator;
import school.hei.haapi.repository.PlaceRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class PlaceService {
    private final PlaceValidator placeValidator;
    private final PlaceRepository placeRepository;

    public Place getById(String placeId){
        return placeRepository.getById(placeId);
    }

    public List<Place> getAll(){
        return placeRepository.findAll();
    }

    public List<Place> saveAll(List<Place> places){
        return placeRepository.saveAll(places);
    }
}
