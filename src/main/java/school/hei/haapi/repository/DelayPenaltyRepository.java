package school.hei.haapi.repository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.DelayPenalty;

@Repository
public interface DelayPenaltyRepository extends JpaRepository<DelayPenalty, String>{
    @java.lang.Override
    default java.util.List<T> findAll(Sort sort) {
        return null;
    }
}
