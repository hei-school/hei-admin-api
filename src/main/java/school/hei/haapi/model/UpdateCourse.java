package school.hei.haapi.model;

import lombok.*;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import static javax.persistence.GenerationType.IDENTITY;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCourse {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private String id;

    public enum Status {
        LINKED, UNLINKED
    }
}
