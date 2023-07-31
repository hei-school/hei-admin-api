package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.Transcript;
import school.hei.haapi.model.TranscriptVersion;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.TranscriptVersionRepository;
import school.hei.haapi.service.aws.S3Service;

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
    //TODO: rename to getStudentIdAndTranscriptId
    public List<TranscriptVersion> getAllVersions(String sId, String tId,PageFromOne page, BoundedPageSize pageSize){
        Pageable pageable = PageRequest.of(
                page.getValue() - 1,
                pageSize.getValue(),
                Sort.by(DESC, "creationDatetime"));
        return repository.getAllByEditorIdAndTranscriptId(sId, tId,pageable);
    }



    public TranscriptVersion addNewTranscriptVersion(String studentId, String transcriptId, String editorId, MultipartFile pdfFile) {
        User student = userService.getById(studentId);
        User editor = userService.getById(editorId);
        //TODO: getByStudentAndId, add studentId and check if studentId equals the studentId in the transcript?
        Transcript transcript = transcriptService.getByStudentIdAndId(transcriptId);

        int newRef = 1;
        if (getAllVersions(studentId,transcriptId, new PageFromOne(1),new BoundedPageSize(10)).size()!=0){
            //newRef = getTranscriptVersion(studentId,transcriptId,"latest").getRef()+1;
            newRef = getTranscriptVersion(transcriptId,"latest").getRef()+1;
        }

        String key = student.getRef()+"-"+transcript.getSemester()+"-"+transcript.getAcademicYear()+"-v"+newRef;
        s3Service.uploadObjectToS3Bucket(key,pdfFile);

        return repository.save(TranscriptVersion.builder()
                .transcript(transcript)
                .ref(newRef)
                .editor(editor)
                .pdfLink(key)
                .build());
    }
}
