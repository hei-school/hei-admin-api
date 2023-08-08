package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.Transcript;
import school.hei.haapi.model.TranscriptVersion;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.TranscriptVersionRepository;
import school.hei.haapi.service.aws.S3Service;

import javax.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Service
@AllArgsConstructor
public class TranscriptVersionService {

    private final TranscriptVersionRepository repository;
    private final S3Service s3Service;
    private final UserService userService;
    private final TranscriptService transcriptService;

    public List<TranscriptVersion> getTranscriptsVersions(String studentId,String transcriptId,PageFromOne page, BoundedPageSize pageSize ){
        Pageable pageable = PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(DESC, "creationDatetime"));
        return repository.findAllByTranscriptStudentIdAndTranscriptId(studentId,transcriptId,pageable);
    }

    public TranscriptVersion getTranscriptVersion(String studentId, String transcriptId, String versionId){
        if(Objects.equals(versionId, "latest")){
            return repository.findFirstByTranscriptStudentIdAndTranscriptIdOrderByRefDesc(studentId,transcriptId)
                    .orElseThrow(() -> new NotFoundException("Transcript's version does not exist"));
        }
        return repository.findByTranscriptStudentIdAndTranscriptIdAndId(studentId,transcriptId,versionId)
                .orElseThrow(() -> new NotFoundException("TranscriptVersion with id "+versionId+" not found"));
    }

    public List<TranscriptVersion> getTranscriptVersionByStudentAndTranscriptId(String sId, String tId,
                                                                                PageFromOne page, BoundedPageSize pageSize){
        Pageable pageable = PageRequest.of(
                page.getValue() - 1,
                pageSize.getValue(),
                Sort.by(DESC, "creationDatetime"));
        return repository.findAllByTranscriptStudentIdAndTranscriptId(sId, tId,pageable);
    }

    public byte[] getTranscriptVersionPdfByStudentIdAndTranscriptIdAndVersionId(String studentId,String transcriptId, String versionId){
        TranscriptVersion transcriptVersion = getTranscriptVersion(studentId,transcriptId,versionId);
        String key = transcriptVersion.getPdfLink();
        if (key.isEmpty()){
            throw new NotFoundException("No pdf attached to the transcriptVersion with id: "+transcriptId);
        }
        return s3Service.getObjectFromS3Bucket(key);
    };
    @Transactional
    public TranscriptVersion addNewTranscriptVersion(String studentId, String transcriptId, String editorId, byte[] pdfFile) {
        Transcript transcript = transcriptService.getByIdAndStudent(transcriptId,studentId);
        if (transcript.getIsDefinitive()){
            throw new BadRequestException("Transcript with id "+transcriptId+" is already definitive");
        }
        User student = userService.getById(studentId);
        User editor = userService.getById(editorId);
        int newRef = 1;
        if (!getTranscriptVersionByStudentAndTranscriptId(studentId, transcriptId, new PageFromOne(1), new BoundedPageSize(10)).isEmpty())
        { newRef = getTranscriptVersion(studentId,transcriptId,"latest").getRef()+1;}
        String key = student.getRef()+"-"+transcript.getAcademicYear()+"-"+transcript.getSemester()+"-v"+newRef+".pdf";
        String pdfKey = s3Service.uploadObjectToS3Bucket(key,pdfFile);
        return repository.save(TranscriptVersion.builder()
                .transcript(transcript)
                .ref(newRef)
                .editor(editor)
                .pdfLink(pdfKey)
                .creationDatetime(Instant.now())
                .build());
    }
}
