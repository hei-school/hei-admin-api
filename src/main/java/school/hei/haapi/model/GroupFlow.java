package school.hei.haapi.model;

import static javax.persistence.CascadeType.REMOVE;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;
import static org.hibernate.annotations.OnDeleteAction.CASCADE;

import java.io.Serializable;
import java.time.Instant;
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
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.Where;
import school.hei.haapi.repository.types.PostgresEnumType;
import school.hei.haapi.service.utils.DataFormatterUtils;

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
@SQLDelete(sql = "update \"group_flow\" set is_deleted = true where id=?")
@Where(clause = "is_deleted=false")
public class GroupFlow implements Serializable {
  // todo: to review all class
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  @ManyToOne(fetch = LAZY, cascade = REMOVE)
  @JoinColumn(name = "student_id")
  @OnDelete(action = CASCADE)
  private User student;

  @ManyToOne
  @JoinColumn(name = "group_id")
  @OnDelete(action = CASCADE)
  private Group group;

  @Column(name = "\"group_flow_type\"")
  @Type(type = "pgsql_enum")
  @Enumerated(EnumType.STRING)
  private group_flow_type groupFlowType;

  private Instant flowDatetime;

  private boolean isDeleted;

  public enum group_flow_type {
    JOIN,
    LEAVE;

    public static group_flow_type fromValue(String value) {
      return DataFormatterUtils.fromValue(group_flow_type.class, value);
    }
  }
}
