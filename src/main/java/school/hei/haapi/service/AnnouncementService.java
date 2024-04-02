package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.model.Scope;
import school.hei.haapi.model.Announcement;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.AnnouncementRepository;
import school.hei.haapi.repository.dao.AnnouncementDao;

import java.time.Instant;
import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Service
@AllArgsConstructor
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final AnnouncementDao announcementDao;

    public Announcement findById(String id){
        return announcementRepository.findById(id).orElseThrow(
                () -> {
                    throw new NotFoundException("Announcement with id #" + id + " not found");
                }
        );
    }

    public List<Announcement> getAnnouncements(Instant from, Instant to, String authorRef, String groupRef, PageFromOne page, BoundedPageSize pageSize, Scope scope){
        Pageable pageable =
                PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(DESC, "creationDatetime"));
        return announcementDao.findByCriteria(from, to, authorRef, scope, groupRef, pageable);
    }
}
