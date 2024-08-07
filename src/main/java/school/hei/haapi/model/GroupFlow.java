package school.hei.haapi.model;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static org.hibernate.type.SqlTypes.NAMED_ENUM;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import school.hei.haapi.service.utils.DataFormatterUtils;

@Entity
@Table(name = "\"group_flow\"")
@Getter
@Setter
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class GroupFlow implements Serializable {
  // todo: to review all class
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "student_id")
  @ToString.Exclude
  private User student;

  @ManyToOne
  @JoinColumn(name = "group_id")
  @ToString.Exclude
  private Group group;

  @Column(name = "\"group_flow_type\"")
  @Enumerated(STRING)
  @JdbcTypeCode(NAMED_ENUM)
  private GroupFlowType groupFlowType;

  private Instant flowDatetime;

  public enum GroupFlowType {
    JOIN,
    LEAVE;

    public static GroupFlowType fromValue(String value) {
      return DataFormatterUtils.fromValue(GroupFlowType.class, value);
    }
  }
}
