package school.hei.haapi.service;


import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.Remedial;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.RemedialRepository;
import school.hei.haapi.repository.dao.RemedialDao;

import java.time.Instant;
import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Service
@AllArgsConstructor
public class RemedialService {
    private final RemedialRepository remedialRepository;
    private final RemedialDao remedialDao;
    public List<Remedial> getAllRemedials(
            PageFromOne page,
            BoundedPageSize pageSize,
            String teacherId,
            String title,
            String courseCode,
            String groupRef,
            Instant remedialDateStart,
            Instant remedialDateEnd) {
        Pageable pageable =
                PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(DESC, "remedialDate"));
        return remedialDao.findByCriteria(
                pageable, teacherId, title, courseCode, groupRef, remedialDateStart, remedialDateEnd);
    }
    public List<Remedial> updateOrSaveAll(List<Remedial> remedials) {
       return remedialRepository.saveAll(remedials);
    }

    public Remedial getRemedialById(String id) {
    return remedialRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Remedial with id #" + id + " not found"));
    }
}
