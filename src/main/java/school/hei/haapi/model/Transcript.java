package school.hei.haapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import school.hei.haapi.repository.types.PostgresEnumType;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.security.Timestamp;
import java.time.Instant;

@Entity
@Table(name = "\"transcript\"")
@TypeDef(name = "pgsql_enum", typeClass = PostgresEnumType.class)
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Builder(toBuilder = true)
public class Transcript {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    private Instant creationDatetime;
    @Type(type = "pgsql_enum")
    @Enumerated(value = EnumType.STRING)
    private Semester semester;
    private Boolean isDefinitive;
    private int academicYear;
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    public enum Semester{
        S1, S2, S3, S4, S5, S6
    }
}
