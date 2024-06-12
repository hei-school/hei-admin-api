package school.hei.haapi.endpoint.event.consumer.model;

import school.hei.haapi.PojaGenerated;

@PojaGenerated
public record TypedEvent(String typeName, Object payload) {}
