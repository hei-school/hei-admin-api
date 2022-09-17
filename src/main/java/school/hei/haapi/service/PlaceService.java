package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.Place;
import school.hei.haapi.repository.PlaceRepository;

import java.util.List;

import static org.springframework.data.domain.Sort.Direction.ASC;

@Service
@AllArgsConstructor
public class PlaceService {
  private final PlaceRepository placeRepository;

  public List<Place> getAll(
      PageFromOne page, BoundedPageSize pageSize
  ){
    Pageable pageable = PageRequest.of(page.getValue() -1, pageSize.getValue(),
      Sort.by(ASC, "name")
    );
    return placeRepository.findAll(pageable).toList();
  }

  public Place getPlaceByName(String name){
    Place place = placeRepository.findByName(name);
    return place;
  }

  public Place getPlaceById (String id){
    Place place = placeRepository.getById(id);
    return place;
  }

  public Place savePlace (Place place) {
    return placeRepository.save(place);
  }

}