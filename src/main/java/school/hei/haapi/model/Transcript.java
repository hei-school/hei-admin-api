package school.hei.haapi.model;

import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "\"transcript\"")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Transcript implements Serializable {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private String id;

    @Type(type = "pgsql_enum")
    @Enumerated(EnumType.STRING)
    private Semester semester;

    @ManyToOne
    @JoinColumn(name = "student_id", referencedColumnName = "id")
    private User student;

    private Integer academic_year;

    private boolean is_definitive;

    @CreationTimestamp
    private Instant creationDatetime;

    @OneToMany(mappedBy = "transcript")
    private List<TranscriptVersion> transcriptVersions;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        Transcript transcript = (Transcript) o;
        return id != null && Objects.equals(id, transcript.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public enum Semester {
        S1, S2, S3, S4, S5, S6
    }
}
