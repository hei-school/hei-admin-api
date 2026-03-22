package school.hei.haapi.model;

import java.time.Instant;
import lombok.Builder;
import school.hei.haapi.model.psp.PspType;

@Builder
public record VolaPayment(
    Integer amount,
    PspType pspType,
    String pspId,
    PaymentStatus status,
    Instant pspLastVerificationInstant,
    Instant creationInstant) {}
