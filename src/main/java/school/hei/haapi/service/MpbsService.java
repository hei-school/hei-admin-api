package school.hei.haapi.service;

import static school.hei.haapi.endpoint.rest.model.MpbsStatus.FAILED;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.PENDING;

import java.util.Comparator;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.Mpbs.Mpbs;
import school.hei.haapi.model.Mpbs.MpbsStatusHistory;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.MpbsRepository;

@Service
@AllArgsConstructor
@Slf4j
public class MpbsService {
  private final MpbsRepository mpbsRepository;
  private final FeeService feeService;

  /**
   * Use MpbsService.saveAll to update mpbs status history
   *
   * @param toSave the mpbs to save
   * @return the saved mpbs
   */
  public List<Mpbs> saveAll(List<Mpbs> toSave) {
    toSave.parallelStream().forEach(MpbsService::updateStatusHistory);
    return mpbsRepository.saveAll(toSave);
  }

  /**
   * Use MpbsService.save to update mpbs status history
   *
   * @param toSave the mpbs to save
   * @return the saved mpbs
   */
  public Mpbs save(Mpbs toSave) {
    return saveAll(List.of(toSave)).getFirst();
  }

  private static void updateStatusHistory(Mpbs mpbs) {
    var statusHistory = mpbs.getStatusHistory();
    var actualSavedStatus =
        statusHistory.stream().max(Comparator.comparing(MpbsStatusHistory::getUpdateInstant));
    if (actualSavedStatus.isPresent()) {
      var presentStatus = actualSavedStatus.get();
      if (presentStatus.getStatus() != mpbs.getStatus()) statusHistory.add(presentStatus);
    } else {
      statusHistory.add(MpbsStatusHistory.fromMpbs(mpbs));
    }
  }

  public Mpbs saveMpbs(Mpbs mobilePaymentByStudentToSave) {
    Fee fee = mobilePaymentByStudentToSave.getFee();
    mobilePaymentByStudentToSave.setFee(feeService.pendFeeForMpbs(fee));
    return save(mobilePaymentByStudentToSave);
  }

  public List<Mpbs> getStudentMobilePaymentByFeeId(String studentId, String feeId) {
    return mpbsRepository.findByStudentIdAndFeeId(studentId, feeId);
  }

  public Mpbs getByPspId(String pspId) {
    return mpbsRepository
        .findByPspId(pspId)
        .orElseThrow(() -> new NotFoundException("Mpbs not found #" + pspId));
  }

  public Long countPendingOfStudent(String studentId) {
    return mpbsRepository.countMpbsByStatusAndStudentId(PENDING, studentId);
  }

  private Mpbs getById(String mpbsId) {
    return mpbsRepository
        .findById(mpbsId)
        .orElseThrow(() -> new NotFoundException("Mpbs not found #" + mpbsId));
  }

  public Mpbs pendFailedMpbs(String mpbsId) {
    var mpbs = getById(mpbsId);
    if (mpbs.getRemainingRetry() <= 0)
      throw new BadRequestException("Mpbs has no remaining retry #" + mpbsId);
    if (FAILED.equals(mpbs.getStatus()))
      throw new BadRequestException("Mpbs must be fail #" + mpbsId);
    mpbs.setStatus(PENDING);
    mpbs.setRemainingRetry(mpbs.getRemainingRetry() - 1);
    return save(mpbs);
  }
}
