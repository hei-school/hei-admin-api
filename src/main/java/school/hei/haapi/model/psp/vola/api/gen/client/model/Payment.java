package school.hei.haapi.model.psp.vola.api.gen.client.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Date;
import java.util.Objects;
import lombok.Builder;

/** Payment */
@Builder
public class Payment {
  @JsonProperty("id")
  private String id = null;

  @JsonProperty("pspPayment")
  private PspPayment pspPayment = null;

  @JsonProperty("creationInstant")
  private Date creationInstant = null;

  @JsonProperty("lastPspVerificationInstant")
  private Date lastPspVerificationInstant = null;

  @JsonProperty("verificationAttemptNb")
  private Integer verificationAttemptNb = null;

  @JsonProperty("payer")
  private User payer = null;

  @JsonProperty("application")
  private Application application = null;

  /** Gets or Sets verificationStatus */
  public enum VerificationStatusEnum {
    VERIFYING("VERIFYING"),
    SUCCEEDED("SUCCEEDED"),
    FAILED("FAILED");

    private String value;

    VerificationStatusEnum(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static VerificationStatusEnum fromValue(String input) {
      for (VerificationStatusEnum b : VerificationStatusEnum.values()) {
        if (b.value.equals(input)) {
          return b;
        }
      }
      return null;
    }
  }

  @JsonProperty("verificationStatus")
  private VerificationStatusEnum verificationStatus = null;

  public Payment id(String id) {
    this.id = id;
    return this;
  }

  /**
   * Get id
   *
   * @return id
   */
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Payment pspPayment(PspPayment pspPayment) {
    this.pspPayment = pspPayment;
    return this;
  }

  /**
   * Get pspPayment
   *
   * @return pspPayment
   */
  public PspPayment getPspPayment() {
    return pspPayment;
  }

  public void setPspPayment(PspPayment pspPayment) {
    this.pspPayment = pspPayment;
  }

  public Payment creationInstant(Date creationInstant) {
    this.creationInstant = creationInstant;
    return this;
  }

  /**
   * Get creationInstant
   *
   * @return creationInstant
   */
  public Date getCreationInstant() {
    return creationInstant;
  }

  public void setCreationInstant(Date creationInstant) {
    this.creationInstant = creationInstant;
  }

  public Payment lastPspVerificationInstant(Date lastPspVerificationInstant) {
    this.lastPspVerificationInstant = lastPspVerificationInstant;
    return this;
  }

  /**
   * Get lastPspVerificationInstant
   *
   * @return lastPspVerificationInstant
   */
  public Date getLastPspVerificationInstant() {
    return lastPspVerificationInstant;
  }

  public void setLastPspVerificationInstant(Date lastPspVerificationInstant) {
    this.lastPspVerificationInstant = lastPspVerificationInstant;
  }

  public Payment verificationAttemptNb(Integer verificationAttemptNb) {
    this.verificationAttemptNb = verificationAttemptNb;
    return this;
  }

  /**
   * Get verificationAttemptNb
   *
   * @return verificationAttemptNb
   */
  public Integer getVerificationAttemptNb() {
    return verificationAttemptNb;
  }

  public void setVerificationAttemptNb(Integer verificationAttemptNb) {
    this.verificationAttemptNb = verificationAttemptNb;
  }

  public Payment payer(User payer) {
    this.payer = payer;
    return this;
  }

  /**
   * Get payer
   *
   * @return payer
   */
  public User getPayer() {
    return payer;
  }

  public void setPayer(User payer) {
    this.payer = payer;
  }

  public Payment application(Application application) {
    this.application = application;
    return this;
  }

  /**
   * Get application
   *
   * @return application
   */
  public Application getApplication() {
    return application;
  }

  public void setApplication(Application application) {
    this.application = application;
  }

  public Payment verificationStatus(VerificationStatusEnum verificationStatus) {
    this.verificationStatus = verificationStatus;
    return this;
  }

  /**
   * Get verificationStatus
   *
   * @return verificationStatus
   */
  public VerificationStatusEnum getVerificationStatus() {
    return verificationStatus;
  }

  public void setVerificationStatus(VerificationStatusEnum verificationStatus) {
    this.verificationStatus = verificationStatus;
  }

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Payment payment = (Payment) o;
    return Objects.equals(this.id, payment.id)
        && Objects.equals(this.pspPayment, payment.pspPayment)
        && Objects.equals(this.creationInstant, payment.creationInstant)
        && Objects.equals(this.lastPspVerificationInstant, payment.lastPspVerificationInstant)
        && Objects.equals(this.verificationAttemptNb, payment.verificationAttemptNb)
        && Objects.equals(this.payer, payment.payer)
        && Objects.equals(this.application, payment.application)
        && Objects.equals(this.verificationStatus, payment.verificationStatus);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        id,
        pspPayment,
        creationInstant,
        lastPspVerificationInstant,
        verificationAttemptNb,
        payer,
        application,
        verificationStatus);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Payment {\n");

    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    pspPayment: ").append(toIndentedString(pspPayment)).append("\n");
    sb.append("    creationInstant: ").append(toIndentedString(creationInstant)).append("\n");
    sb.append("    lastPspVerificationInstant: ")
        .append(toIndentedString(lastPspVerificationInstant))
        .append("\n");
    sb.append("    verificationAttemptNb: ")
        .append(toIndentedString(verificationAttemptNb))
        .append("\n");
    sb.append("    payer: ").append(toIndentedString(payer)).append("\n");
    sb.append("    application: ").append(toIndentedString(application)).append("\n");
    sb.append("    verificationStatus: ").append(toIndentedString(verificationStatus)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
