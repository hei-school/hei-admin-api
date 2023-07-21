package school.hei.haapi.model;

import lombok.*;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;
@Entity
@Table(name = "\"transcript_version\"")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Version {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private String id;

    @ManyToOne
    @JoinColumn(name = "transcript", referencedColumnName = "id")
    private Transcript transcript_id;
    @Column(unique = true)
    private String ref;
    @ManyToOne
    @JoinColumn(name = "user", referencedColumnName = "id")
    private  User createBy;
    private Date creation_datetime;
}
