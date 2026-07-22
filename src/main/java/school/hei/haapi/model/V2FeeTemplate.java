package school.hei.haapi.model;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.EAGER;
import static java.time.Instant.now;
import static org.hibernate.type.SqlTypes.NAMED_ENUM;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import school.hei.haapi.endpoint.rest.model.FeeCategory;
import school.hei.haapi.endpoint.rest.model.FeeTypeEnum;

@Entity
@Table(name = "\"v2_fee_template\"")
@Getter
@Setter
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class V2FeeTemplate {
  @Id private String id;

  private String label;

  @JdbcTypeCode(NAMED_ENUM)
  @Enumerated(STRING)
  private FeeTypeEnum type;

  @JdbcTypeCode(NAMED_ENUM)
  @Enumerated(STRING)
  private FeeCategory category;

  @OneToMany(mappedBy = "feeTemplate", cascade = ALL, fetch = EAGER)
  private List<V2FeeTemplateContent> feeTemplateContents;

  @CreationTimestamp private Instant creationDatetime;

  @PrePersist
  private void prePersist() {
    this.creationDatetime = now();
  }
}
