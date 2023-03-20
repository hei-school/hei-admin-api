package io.swagger.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.model.CreateDelayPenaltyChange;
import io.swagger.v3.oas.annotations.media.Schema;
import org.threeten.bp.OffsetDateTime;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * DelayPenalty
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2023-03-20T17:20:51.776723221Z[GMT]")


public class DelayPenalty extends CreateDelayPenaltyChange  {
  @JsonProperty("id")
  private String id = null;

  @JsonProperty("creation_datetime")
  private OffsetDateTime creationDatetime = null;

  public DelayPenalty id(String id) {
    this.id = id;
    return this;
  }

  /**
   * Get id
   * @return id
   **/
  @Schema(description = "")
  
    public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public DelayPenalty creationDatetime(OffsetDateTime creationDatetime) {
    this.creationDatetime = creationDatetime;
    return this;
  }

  /**
   * Get creationDatetime
   * @return creationDatetime
   **/
  @Schema(description = "")
  
    @Valid
    public OffsetDateTime getCreationDatetime() {
    return creationDatetime;
  }

  public void setCreationDatetime(OffsetDateTime creationDatetime) {
    this.creationDatetime = creationDatetime;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DelayPenalty delayPenalty = (DelayPenalty) o;
    return Objects.equals(this.id, delayPenalty.id) &&
        Objects.equals(this.creationDatetime, delayPenalty.creationDatetime) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, creationDatetime, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DelayPenalty {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    creationDatetime: ").append(toIndentedString(creationDatetime)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
