package school.hei.haapi.model.psp.vola.api.gen.client.model;

import lombok.Builder;
import school.hei.haapi.model.psp.PspType;

@Builder
public record PaymentInfo(String payerEmail, PspType pspType, String pspPaymentId) {}
