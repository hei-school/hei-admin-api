package io.swagger.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import org.threeten.bp.OffsetDateTime;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * CreateFee
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2023-03-20T17:20:51.776723221Z[GMT]")


public class CreateFee   {
  /**
   * Gets or Sets type
   */
  public enum TypeEnum {
    TUITION("TUITION"),
    
    HARDWARE("HARDWARE");

    private String value;

    TypeEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static TypeEnum fromValue(String text) {
      for (TypeEnum b : TypeEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }
  @JsonProperty("type")
  private TypeEnum type = null;

  @JsonProperty("comment")
  private String comment = null;

  @JsonProperty("total_amount")
  private Integer totalAmount = null;

  @JsonProperty("creation_datetime")
  private OffsetDateTime creationDatetime = null;

  @JsonProperty("due_datetime")
  private OffsetDateTime dueDatetime = null;

  public CreateFee type(TypeEnum type) {
    this.type = type;
    return this;
  }

  /**
   * Get type
   * @return type
   **/
  @Schema(description = "")
  
    public TypeEnum getType() {
    return type;
  }

  public void setType(TypeEnum type) {
    this.type = type;
  }

  public CreateFee comment(String comment) {
    this.comment = comment;
    return this;
  }

  /**
   * Get comment
   * @return comment
   **/
  @Schema(description = "")
  
    public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public CreateFee totalAmount(Integer totalAmount) {
    this.totalAmount = totalAmount;
    return this;
  }

  /**
   * Get totalAmount
   * @return totalAmount
   **/
  @Schema(description = "")
  
    public Integer getTotalAmount() {
    return totalAmount;
  }

  public void setTotalAmount(Integer totalAmount) {
    this.totalAmount = totalAmount;
  }

  public CreateFee creationDatetime(OffsetDateTime creationDatetime) {
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

  public CreateFee dueDatetime(OffsetDateTime dueDatetime) {
    this.dueDatetime = dueDatetime;
    return this;
  }

  /**
   * Get dueDatetime
   * @return dueDatetime
   **/
  @Schema(description = "")
  
    @Valid
    public OffsetDateTime getDueDatetime() {
    return dueDatetime;
  }

  public void setDueDatetime(OffsetDateTime dueDatetime) {
    this.dueDatetime = dueDatetime;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CreateFee createFee = (CreateFee) o;
    return Objects.equals(this.type, createFee.type) &&
        Objects.equals(this.comment, createFee.comment) &&
        Objects.equals(this.totalAmount, createFee.totalAmount) &&
        Objects.equals(this.creationDatetime, createFee.creationDatetime) &&
        Objects.equals(this.dueDatetime, createFee.dueDatetime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, comment, totalAmount, creationDatetime, dueDatetime);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreateFee {\n");
    
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    comment: ").append(toIndentedString(comment)).append("\n");
    sb.append("    totalAmount: ").append(toIndentedString(totalAmount)).append("\n");
    sb.append("    creationDatetime: ").append(toIndentedString(creationDatetime)).append("\n");
    sb.append("    dueDatetime: ").append(toIndentedString(dueDatetime)).append("\n");
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
