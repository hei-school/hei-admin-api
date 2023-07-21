package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Transcript;
import school.hei.haapi.repository.TranscriptRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class TranscriptService {
    private TranscriptRepository transcriptRepository;

    public Transcript getByStudentIdAndId(String studentId, String id) {
        return transcriptRepository.getByStudentIdAndId(studentId, id);
    }

    public List<Transcript> getAllByStudentId(String studentId) {
        return transcriptRepository.findAllByStudentId(studentId);
    }
}
