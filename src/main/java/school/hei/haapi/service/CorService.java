package school.hei.haapi.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.model.CorNotificationRequested;
import school.hei.haapi.endpoint.rest.mapper.CorMapper;
import school.hei.haapi.endpoint.rest.model.CrupdateCor;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Cor;
import school.hei.haapi.model.CorStatus;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.exception.BadRequestException;
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
  private final EventProducer<CorNotificationRequested> eventProducer;
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

  public Cor save(String studentId, CrupdateCor dto) {
    Cor cor =
        Optional.ofNullable(dto.getId())
            .flatMap(corRepository::findById)
            .map(
                existing -> {
                  if (!existing.getStudent().getId().equals(studentId)) {
                    throw new BadRequestException("Mismatch between studentId and Cor's student");
                  }
                  return corMapper.toDomainUpdate(dto, existing);
                })
            .orElseGet(
                () -> {
                  Cor created = corMapper.toDomain(dto);
                  if (!created.getStudent().getId().equals(studentId)) {
                    throw new BadRequestException("Mismatch between studentId and Cor's student");
                  }
                  return created;
                });
    Cor saved = corRepository.save(cor);
    eventProducer.accept(List.of(new CorNotificationRequested(saved.getId())));
    return saved;
  }

  public Cor getById(String id) {
    return corRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Cor with id # %s not found".formatted(id)));
  }
}
