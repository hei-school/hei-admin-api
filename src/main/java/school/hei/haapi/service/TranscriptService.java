package school.hei.haapi.service;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Transcript;
import school.hei.haapi.repository.TranscriptRepository;

@Service
@AllArgsConstructor
public class TranscriptService {
  private final TranscriptRepository repository;

  public List<Transcript> crupdateTranscripts(List<Transcript> transcripts) {
    return repository.saveAll(transcripts);
  }
}
