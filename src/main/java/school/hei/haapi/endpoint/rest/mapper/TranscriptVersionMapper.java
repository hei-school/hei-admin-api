package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Manager;
import school.hei.haapi.endpoint.rest.model.Transcript;
import school.hei.haapi.endpoint.rest.model.TranscriptVersion;
import school.hei.haapi.service.TranscriptVersionService;

@Component
@AllArgsConstructor
public class TranscriptVersionMapper {
    private final TranscriptVersionService service;
    private final UserMapper userMapper;
    private final TranscriptMapper transcriptMapper;

    public TranscriptVersion toRest(school.hei.haapi.model.TranscriptVersion version) {
        Manager user = userMapper.toRestManager(version.getUserId());
        Transcript transcript = transcriptMapper.toRest(version.getTranscriptId());
        return new TranscriptVersion()
                .id(version.getId())
                .ref(version.getRef())
                .creationDatetime(version.getCreationDatetime())
                .transcriptId(transcript)
                .userId(user);
    }
    public TranscriptVersion toRestURl(school.hei.haapi.model.TranscriptVersion version) {
        Manager user = userMapper.toRestManager(version.getUserId());
        Transcript transcript = transcriptMapper.toRest(version.getTranscriptId());
        return new TranscriptVersion()
                .id(version.getId())
                .ref(version.getRef())
                .pdfLink(version.getPdf_link())
                .creationDatetime(version.getCreationDatetime())
                .transcriptId(transcript)
                .userId(user);
    }
}
