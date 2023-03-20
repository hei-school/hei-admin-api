package io.swagger.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * CreateDelayPenaltyChange
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2023-03-20T17:20:51.776723221Z[GMT]")


public class CreateDelayPenaltyChange   {
  @JsonProperty("interest_percent")
  private Integer interestPercent = null;

  /**
   * Gets or Sets interestTimerate
   */
  public enum InterestTimerateEnum {
    DAILY("DAILY");

    private String value;

    InterestTimerateEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static InterestTimerateEnum fromValue(String text) {
      for (InterestTimerateEnum b : InterestTimerateEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }
  @JsonProperty("interest_timerate")
  private InterestTimerateEnum interestTimerate = null;

  @JsonProperty("grace_delay")
  private Integer graceDelay = null;

  @JsonProperty("applicability_delay_after_grace")
  private Integer applicabilityDelayAfterGrace = null;

  public CreateDelayPenaltyChange interestPercent(Integer interestPercent) {
    this.interestPercent = interestPercent;
    return this;
  }

  /**
   * Get interestPercent
   * @return interestPercent
   **/
  @Schema(description = "")
  
    public Integer getInterestPercent() {
    return interestPercent;
  }

  public void setInterestPercent(Integer interestPercent) {
    this.interestPercent = interestPercent;
  }

  public CreateDelayPenaltyChange interestTimerate(InterestTimerateEnum interestTimerate) {
    this.interestTimerate = interestTimerate;
    return this;
  }

  /**
   * Get interestTimerate
   * @return interestTimerate
   **/
  @Schema(description = "")
  
    public InterestTimerateEnum getInterestTimerate() {
    return interestTimerate;
  }

  public void setInterestTimerate(InterestTimerateEnum interestTimerate) {
    this.interestTimerate = interestTimerate;
  }

  public CreateDelayPenaltyChange graceDelay(Integer graceDelay) {
    this.graceDelay = graceDelay;
    return this;
  }

  /**
   * Get graceDelay
   * @return graceDelay
   **/
  @Schema(description = "")
  
    public Integer getGraceDelay() {
    return graceDelay;
  }

  public void setGraceDelay(Integer graceDelay) {
    this.graceDelay = graceDelay;
  }

  public CreateDelayPenaltyChange applicabilityDelayAfterGrace(Integer applicabilityDelayAfterGrace) {
    this.applicabilityDelayAfterGrace = applicabilityDelayAfterGrace;
    return this;
  }

  /**
   * Get applicabilityDelayAfterGrace
   * @return applicabilityDelayAfterGrace
   **/
  @Schema(description = "")
  
    public Integer getApplicabilityDelayAfterGrace() {
    return applicabilityDelayAfterGrace;
  }

  public void setApplicabilityDelayAfterGrace(Integer applicabilityDelayAfterGrace) {
    this.applicabilityDelayAfterGrace = applicabilityDelayAfterGrace;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CreateDelayPenaltyChange createDelayPenaltyChange = (CreateDelayPenaltyChange) o;
    return Objects.equals(this.interestPercent, createDelayPenaltyChange.interestPercent) &&
        Objects.equals(this.interestTimerate, createDelayPenaltyChange.interestTimerate) &&
        Objects.equals(this.graceDelay, createDelayPenaltyChange.graceDelay) &&
        Objects.equals(this.applicabilityDelayAfterGrace, createDelayPenaltyChange.applicabilityDelayAfterGrace);
  }

  @Override
  public int hashCode() {
    return Objects.hash(interestPercent, interestTimerate, graceDelay, applicabilityDelayAfterGrace);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreateDelayPenaltyChange {\n");
    
    sb.append("    interestPercent: ").append(toIndentedString(interestPercent)).append("\n");
    sb.append("    interestTimerate: ").append(toIndentedString(interestTimerate)).append("\n");
    sb.append("    graceDelay: ").append(toIndentedString(graceDelay)).append("\n");
    sb.append("    applicabilityDelayAfterGrace: ").append(toIndentedString(applicabilityDelayAfterGrace)).append("\n");
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
