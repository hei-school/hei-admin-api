package school.hei.haapi.service;

import jakarta.transaction.Transactional;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.model.MpbsStatus;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.model.mpbs.Mpbs;
import school.hei.haapi.model.mpbs.MpbsStatusHistory;
import school.hei.haapi.repository.MpbsRepository;

@Service
@AllArgsConstructor
@Slf4j
public class MpbsService {
  private final MpbsRepository mpbsRepository;
  private final FeeService feeService;

  @Transactional
  public Mpbs saveVerifiedSuccessfulPayment(Mpbs verifiedMpbs) {
    var lockedMpbs =
        mpbsRepository
            .findByIdForUpdate(verifiedMpbs.getId())
            .orElseThrow(() -> new NotFoundException("Mpbs not found #" + verifiedMpbs.getId()));
    if (!MpbsStatus.PENDING.equals(lockedMpbs.getStatus())) {
      log.info(
          "Mpbs {} was already resolved to {} while waiting for the lock, skipping",
          verifiedMpbs.getId(),
          lockedMpbs.getStatus());
      return lockedMpbs;
    }
    feeService.computeRemainingAmount(verifiedMpbs.getFee().getId(), verifiedMpbs.getAmount());
    return save(verifiedMpbs);
  }

  public List<Mpbs> saveAll(List<Mpbs> toSave) {
    toSave.parallelStream().forEach(MpbsService::updateStatusHistory);
    return mpbsRepository.saveAll(toSave);
  }

  public Mpbs save(Mpbs toSave) {
    return saveAll(List.of(toSave)).getFirst();
  }

  private static void updateStatusHistory(Mpbs mpbs) {
    var lastHistoryOpt = mpbs.getLastStatusHistory();
    var statusNotChanged =
        lastHistoryOpt.map(history -> history.getStatus() == mpbs.getStatus()).orElse(false);

    if (statusNotChanged) {
      return;
    }

    mpbs.getStatusHistory().add(MpbsStatusHistory.fromMpbs(mpbs));
  }

  public Mpbs saveMpbs(Mpbs mobilePaymentByStudentToSave) {
    var fee = mobilePaymentByStudentToSave.getFee();
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
    return mpbsRepository.countMpbsByStatusAndStudentId(MpbsStatus.PENDING, studentId);
  }
}
