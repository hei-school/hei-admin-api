package school.hei.haapi.handler.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import school.hei.haapi.PojaGenerated;

@PojaGenerated
public record ErrorModel(@JsonProperty("message") String message) {}
