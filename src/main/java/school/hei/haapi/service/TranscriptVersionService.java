package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.TranscriptVersion;
import school.hei.haapi.repository.TranscriptVersionRepository;
import java.util.List;
import java.util.Objects;
import static org.springframework.data.domain.Sort.Direction.DESC;

@Service
@AllArgsConstructor
public class TranscriptVersionService {

    private final TranscriptVersionRepository repository;

    public List<TranscriptVersion> getTranscriptsVersions(String transcriptId,PageFromOne page, BoundedPageSize pageSize ){
        Pageable pageable = PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(DESC, "creationDatetime"));
        return repository.findAllByTranscriptId(transcriptId,pageable);
    }

    public TranscriptVersion getTranscriptVersion(String transcriptId, String versionId){
        if(Objects.equals(versionId, "latest")){
            return repository.findFirstByTranscriptIdOrderByCreationDatetimeDesc(transcriptId);
        }
        return repository.getById(versionId);
    }

    public void getTranscriptVersionPdf(String tId,String vId){};
}
