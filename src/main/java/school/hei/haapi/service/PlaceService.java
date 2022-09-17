package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.Place;
import school.hei.haapi.model.validator.PlaceValidator;
import school.hei.haapi.repository.PlaceRepository;

import java.util.Arrays;
import java.util.List;

import static org.springframework.data.domain.Sort.Direction.ASC;

@Service
@AllArgsConstructor
public class PlaceService {
    private final PlaceValidator placeValidator;
    private final PlaceRepository placeRepository;

    public Place getById(String placeId){
        return placeRepository.getById(placeId);
    }

    /*public List<Place> getAll(){
        return placeRepository.findAll();
    }*/


    public List<Place> saveAll(List<Place> places){
        return placeRepository.saveAll(places);
    }

    public List<Place> getAll(PageFromOne page,
                              BoundedPageSize pageSize,
                              String name) {
            Pageable pageable = PageRequest.of(
                    page.getValue() - 1,
                    pageSize.getValue(),
                    Sort.by(ASC, "name"));
        return placeRepository.findByNameContainingIgnoreCase(name,pageable);
    }
}
