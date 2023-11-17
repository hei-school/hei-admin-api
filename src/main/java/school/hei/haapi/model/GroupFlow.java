package school.hei.haapi.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import school.hei.haapi.repository.types.PostgresEnumType;
import school.hei.haapi.service.utils.DataFormatterUtils;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "\"group_flow\"")
@Getter
@Setter
@TypeDef(name = "pgsql_enum", typeClass = PostgresEnumType.class)
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class GroupFlow implements Serializable {
  //todo: to review all class
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "student_id")
  private User student;

  @ManyToOne
  @JoinColumn(name = "group_id")
  private Group group;

  @Column(name = "\"group_flow_type\"")
  @Type(type = "pgsql_enum")
  @Enumerated(EnumType.STRING)
  private group_flow_type groupFlowType;

  private Instant flowDatetime;

  public enum group_flow_type {
    JOIN, LEAVE;
    public static group_flow_type fromValue(String value) {
      return DataFormatterUtils.fromValue(group_flow_type.class, value);
    }
  }
}
