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
    public  Version getRawTranscript(long studentId, String transcriptId, int versionId){
        Version version = new Version();
        version.setId(version.getId());
        version.setTranscript_id(version.getTranscript_id());
        version.setRef(version.getRef());
        version.setCreateBy(version.getCreateBy());
        version.setCreation_datetime(version.getCreation_datetime());
        return version;
    }
}
