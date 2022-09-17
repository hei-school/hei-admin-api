package school.hei.haapi.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Present {
    private String s3name;
    private String studentId;
    private Float similarity;
    private Float boundingBoxWidth;
    private Float boundingBoxTop;
    private Float boundingBoxLeft;
    private Float boundingBoxHeight;
}
