package school.hei.haapi.endpoint.event.model;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@Builder
@ToString
@AllArgsConstructor
public class SendUnpaidFeesReminderTriggered implements Serializable {}
