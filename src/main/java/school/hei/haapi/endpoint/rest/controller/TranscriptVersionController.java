package school.hei.haapi.endpoint.rest.controller;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import lombok.AllArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import school.hei.haapi.endpoint.rest.mapper.TranscriptVersionMapper;
import school.hei.haapi.model.Transcript;
import school.hei.haapi.model.Version;
import school.hei.haapi.service.TranscriptVersionService;
import com.itextpdf.text.pdf.PdfWriter;
import javax.servlet.http.HttpServletResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.format.DateTimeFormatter;

@RestController
@AllArgsConstructor
@RequestMapping("/students/{sId}/transcripts/{tId}/versions/{vId}")
public class TranscriptVersionController {
    private TranscriptVersionService transcriptVersionService;
    private TranscriptVersionMapper transcriptMapper;
    @GetMapping("/raw")
    public ResponseEntity<byte[]> generatePdf(
            @RequestParam String studentId,
            @RequestParam Transcript transcriptId,
            @RequestParam String versionId
    ) {
        // Generate the PDF using the mapper
        ByteArrayOutputStream outputStream = transcriptMapper.getRawTranscriptPdf(studentId, transcriptId, versionId);

        // Convert the ByteArrayOutputStream to a byte array
        byte[] pdfBytes = outputStream.toByteArray();

        // Set the necessary headers in the response
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "transcript.pdf");

        // Return the PDF as a ResponseEntity
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}

