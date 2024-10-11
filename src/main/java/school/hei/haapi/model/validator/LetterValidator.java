package school.hei.haapi.model.validator;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.Letter;
import school.hei.haapi.model.exception.BadRequestException;

import java.util.function.Consumer;

@Component
@AllArgsConstructor
public class LetterValidator implements Consumer<Letter> {
    @Override
    public void accept(Letter letter) {
        if (letter.getAmount() == null && letter.getFee() == null) {
            throw new BadRequestException("Must provide amount if the letter is linked with fee");
        }
        if (letter.getDescription() == null || letter.getDescription().trim().isEmpty()) {
            throw new BadRequestException("Description must not be blank");
        }
        if (letter.getFilePath() == null) {
            throw new BadRequestException("Path must not be blank");
        }
    }
}
