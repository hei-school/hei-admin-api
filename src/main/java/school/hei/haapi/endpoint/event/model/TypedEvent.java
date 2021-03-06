package school.hei.haapi.endpoint.event.model;

import java.io.Serializable;

/**
 * Event models generated by EventBridge are NOT typed, unfortunately.
 * TypedEvent takes care of that.
 */
public interface TypedEvent {
  String getTypeName();

  Serializable getPayload();
}
