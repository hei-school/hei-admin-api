package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Place;

@Repository
public interface PlaceRepository extends JpaRepository<Place, String> {
    Place findByNameContainingIgnoreCase(String name);
}
