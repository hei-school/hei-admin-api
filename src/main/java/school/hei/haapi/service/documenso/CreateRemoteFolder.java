package school.hei.haapi.service.documenso;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CreateRemoteFolder(
    @JsonProperty("name") String name,
    @JsonProperty("parentId") String parentId,
    @JsonProperty("type") String type) {}
