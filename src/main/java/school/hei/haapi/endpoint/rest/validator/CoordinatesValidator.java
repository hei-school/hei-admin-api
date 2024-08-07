package school.hei.haapi.endpoint.rest.validator;

import java.util.Objects;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Coordinates;
import school.hei.haapi.model.exception.BadRequestException;

@Component
public class CoordinatesValidator implements Consumer<Coordinates> {
  @Override
  public void accept(Coordinates coordinates) {
    if (Objects.isNull(coordinates.getLongitude()) && !Objects.isNull(coordinates.getLatitude())) {
      throw new BadRequestException("Longitude is null, it must go hand in hand with latitude");
    }
    if (!Objects.isNull(coordinates.getLongitude()) && Objects.isNull(coordinates.getLatitude())) {
      throw new BadRequestException("Latitude is null, it must go hand in hand with longitude");
    }
  }
}
