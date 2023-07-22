package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Version;
import school.hei.haapi.repository.TranscriptVersionRepository;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class TranscriptVersionService {
    private final TranscriptVersionRepository transcriptVersionRepository;
    public Version getById(String versionId){
        return transcriptVersionRepository.getById(versionId);
    }
    public  Version getRawTranscript(String studentId, String transcriptId, String versionId){
        return transcriptVersionRepository.getById(versionId);
    }
}
