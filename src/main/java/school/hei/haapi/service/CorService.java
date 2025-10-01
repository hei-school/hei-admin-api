package school.hei.haapi.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.model.CorNotification;
import school.hei.haapi.endpoint.rest.mapper.CorMapper;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Cor;
import school.hei.haapi.model.CorStatus;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.model.pagination.PaginationFromPageAndPageSize;
import school.hei.haapi.repository.CorRepository;
import school.hei.haapi.repository.dao.CorDao;

@Service
@AllArgsConstructor
public class CorService {
  private final CorRepository corRepository;
  private final PaginationFromPageAndPageSize paginationFromPageAndPageSize;
  private final CorDao corDao;
  private final EventProducer<CorNotification> corNotification;
  private final CorMapper corMapper;

  public List<Cor> getCors(
      Instant from,
      Instant to,
      String studentRef,
      String groupRef,
      List<CorStatus> statuses,
      Pageable pageable) {
    return corDao.findByCriteria(from, to, studentRef, groupRef, statuses, pageable);
  }

  public List<Cor> findAllByStudentId(String studentId, PageFromOne page, BoundedPageSize size) {
    return corRepository.findAllByStudentId(
        studentId, paginationFromPageAndPageSize.apply(page, size));
  }

  public Cor save(Cor cor) {
    if (findById(cor.getId()).isEmpty()) {
      corNotification.accept(List.of(new CorNotification(corMapper.toRest(cor))));
    }
    // TODO: save the Cor before send it to the event
    return corRepository.save(cor);
  }

  public Cor getById(String id) {
    return findById(id)
        .orElseThrow(() -> new NotFoundException("Cor with id # %s not found".formatted(id)));
  }

  private Optional<Cor> findById(String id) {
    if (id == null) return Optional.empty();
    return corRepository.findById(id);
  }
}
