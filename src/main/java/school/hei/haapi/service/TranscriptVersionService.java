package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.*;
import school.hei.haapi.repository.TranscriptVersionRepository;

import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Service
@AllArgsConstructor
public class TranscriptVersionService {

    private final TranscriptVersionRepository repository;

    public List<TranscriptVersion> getAllVersions(String sId, String tId,PageFromOne page, BoundedPageSize pageSize){
        Pageable pageable = PageRequest.of(
                page.getValue() - 1,
                pageSize.getValue(),
                Sort.by(DESC, "creationDatetime"));
        return repository.getAllByEditorIdAndTranscriptId(sId, tId,pageable);
    }

    public TranscriptVersion getTranscriptVersion(String sId, String tId, String vId){
        if(vId == "latest"){
            return repository.getAllSortedByRef(sId,tId).get(0);
        }
        return repository.getTranscriptVersionByEditorIdAndTranscriptIdAndId(sId,tId,vId);
    }
}
