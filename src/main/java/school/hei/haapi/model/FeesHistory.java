package school.hei.haapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.TypeDef;
import org.springframework.boot.context.properties.bind.DefaultValue;
import school.hei.haapi.repository.types.PostgresEnumType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "\"fee_history\"")
@Getter
@Setter
@EqualsAndHashCode
@TypeDef(name = "pgsql_enum", typeClass = PostgresEnumType.class)
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class FeesHistory {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private String id;

  @NotNull
  @Column(nullable = false)
  private Double fee_total = 0.0;

  @NotNull
  @Column(nullable = false)
  private Boolean paid = false;

  @NotNull
  @Column(nullable = false)
  private int percentage = 0;

  @NotNull
  @Column(nullable = false , name = "grace_delay_student")
  private int graceStudentDelay = 0;

  @OneToOne
  @JoinColumn(name = "user_id")
  private User student;
}
