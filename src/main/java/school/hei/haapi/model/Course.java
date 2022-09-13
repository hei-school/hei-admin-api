package school.hei.haapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

import static javax.persistence.GenerationType.IDENTITY;
@Entity
@Table(name = "\"course\"")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Course implements Serializable{
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private String id;

    @NotBlank(message = "Reference is mandatory")
    private String ref;

    @NotBlank(message = "Name is mandatory")
    private String name;

    @Min(value = 1, message = "Credits cannot be negative or equal to 0")
    @NotNull(message = "Credits is mandatory")
    private int credits;

    @NotNull(message = "Total hours is mandatory")
    @Min(value = 1,message = "Total hours cannot be negative or equal to 0")
    private int totalHours;
}
