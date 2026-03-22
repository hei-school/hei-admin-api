package school.hei.haapi.endpoint.rest.controller;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.model.VerifyMpbsByXlsEvent;
import school.hei.haapi.endpoint.rest.mapper.MpbsMapper;
import school.hei.haapi.endpoint.rest.mapper.VolaMapper;
import school.hei.haapi.endpoint.rest.model.CrupdateMpbs;
import school.hei.haapi.endpoint.rest.model.Mpbs;
import school.hei.haapi.endpoint.rest.model.MpbsStatus;
import school.hei.haapi.endpoint.rest.validator.CreateMpbsValidator;
import school.hei.haapi.model.psp.vola.VolaPsp;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.service.MpbsService;
import school.hei.haapi.service.MpbsVerificationService;
import school.hei.haapi.service.MultipartFileConverter;

@RestController
@AllArgsConstructor
public class MpbsController {
  private final CreateMpbsValidator validator;
  private final MpbsService mpbsService;
  private final MpbsMapper mapper;
  private final MultipartFileConverter multipartFileConverter;
  private final EventProducer eventProducer;
  private final MpbsVerificationService mpbsVerificationService;
  private final VolaPsp volaPsp;
  private final VolaMapper volaMapper;
  private final UserRepository userRepository;

  @PutMapping(value = "/students/{student_id}/fees/{fee_id}/mpbs")
  public Mpbs crupdateMpbs(
      @PathVariable(name = "student_id") String studentId,
      @PathVariable(name = "fee_id") String feeId,
      @RequestBody CrupdateMpbs mpbsToSave) {
    validator.accept(studentId, feeId, mpbsToSave);
    school.hei.haapi.model.mpbs.Mpbs mappedMpbsToSave = mapper.toDomain(mpbsToSave);
    var volaPaymentResponse =
        volaPsp.create(
            volaMapper.toPspType(mappedMpbsToSave.getMobileMoneyType()),
            mappedMpbsToSave.getPspId(),
            mappedMpbsToSave.getStudent().getEmail());
    var mpbsMappedFromVola = volaMapper.toMpbs(mappedMpbsToSave, volaPaymentResponse);

    return mapper.toRest(mpbsService.saveMpbs(mpbsMappedFromVola));
  }

  @GetMapping(value = "/students/{student_id}/fees/{fee_id}/mpbs")
  public List<Mpbs> getMpbs(
      @PathVariable(name = "student_id") String studentId,
      @PathVariable(name = "fee_id") String feeId) {
    var mpbsListForTheFee =
        mpbsService.getStudentMobilePaymentByFeeId(studentId, feeId).stream().toList();
    var mpbsListForTheFeeChecked =
        mpbsListForTheFee.stream()
            .filter(mpbs -> mpbs.getStatus() == MpbsStatus.PENDING)
            .map(mpbsVerificationService::checkIfVerifiedFromVola)
            .toList();
    var listWithoutPending =
        mpbsListForTheFee.stream()
            .filter(mpbs -> !(mpbs.getStatus() == MpbsStatus.PENDING))
            .toList();
    listWithoutPending.addAll(mpbsListForTheFeeChecked);
    return listWithoutPending.stream().map(mapper::toRest).toList();
  }

  @PostMapping(value = "/mpbs/verify", consumes = MULTIPART_FORM_DATA_VALUE)
  public List<Mpbs> verifyMpbs(@RequestPart(name = "file_to_upload") MultipartFile file)
      throws IOException {
    eventProducer.accept(
        List.of(
            VerifyMpbsByXlsEvent.builder()
                .fileKey(mpbsVerificationService.uploadXlsToS3(file))
                .verificationInstant(Instant.now())
                .build()));
    return List.of();
  }
}
