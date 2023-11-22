package school.hei.haapi.endpoint.event.gen;

import java.io.Serializable;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@Builder
@ToString
public class SendLateFeesEmailTriggered implements Serializable {}
