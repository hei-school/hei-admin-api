package school.hei.haapi.endpoint.rest.controller;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import lombok.AllArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.model.Version;
import school.hei.haapi.service.TranscriptVersionService;
import com.itextpdf.text.pdf.PdfWriter;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.time.format.DateTimeFormatter;

@RestController
@AllArgsConstructor
@RequestMapping("/students/{sId}/transcripts/{tId}/versions/{vId}")
public class TranscriptVersionController {
    private TranscriptVersionService transcriptVersionService;
    @GetMapping("/raw")
    public void getRawTranscript(@PathVariable("sId") long studentId,
                                 @PathVariable("tId") String transcriptId,
                                 @PathVariable("vId") int versionId,
                                 HttpServletResponse response) throws IOException, DocumentException {
        Version version = transcriptVersionService.getRawTranscript(studentId, transcriptId, versionId);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=transcript.pdf");

        OutputStream out = response.getOutputStream();
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();
            document.add(new Paragraph("ID: " + version.getId()));
            document.add(new Paragraph("Transcript ID: " + version.getTranscript_id()));
            document.add(new Paragraph("Ref: " + version.getRef()));
            document.add(new Paragraph("Created By: " + version.getCreateBy()));
            document.add(new Paragraph(String.format("Creation Datetime: " + version.getCreation_datetime(), DateTimeFormatter.ISO_DATE_TIME)));
            document.close();
        } catch (DocumentException e) {
            e.printStackTrace();
        } finally {
            out.flush();
            out.close();
        }
    }
}
