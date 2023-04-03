package school.hei.haapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.TypeDef;
import school.hei.haapi.repository.types.PostgresEnumType;

import javax.persistence.*;
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

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User student;
}
