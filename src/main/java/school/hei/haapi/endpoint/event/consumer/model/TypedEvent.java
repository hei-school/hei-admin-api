package school.hei.haapi.endpoint.event.consumer.model;

import school.hei.haapi.PojaGenerated;
import school.hei.haapi.endpoint.event.model.PojaEvent;

@PojaGenerated
@SuppressWarnings("all")
public record TypedEvent(String typeName, PojaEvent payload) {}
