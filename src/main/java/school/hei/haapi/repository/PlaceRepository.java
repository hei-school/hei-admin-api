package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import school.hei.haapi.model.Place;

public interface PlaceRepository extends JpaRepository<Place,String> {
}
