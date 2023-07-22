package school.hei.haapi.endpoint.rest.mapper;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.Transcript;
import school.hei.haapi.model.Version;


import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Component
@AllArgsConstructor
public class TranscriptVersionMapper {

    public ByteArrayOutputStream getRawTranscriptPdf(String studentId, Transcript transcriptId, String versionId){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Version version = new Version();
        version.setId(versionId);
        version.setTranscript_id(transcriptId);
        version.setRef(version.getRef());
        version.setCreateBy(version.getCreateBy());
        version.setCreation_datetime(version.getCreation_datetime());
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);
            document.open();
            document.add(new Paragraph("ID: " + version.getId()));
            document.add(new Paragraph("Transcript ID: " + version.getTranscript_id()));
            document.add(new Paragraph("Ref: " + version.getRef()));
            document.add(new Paragraph("Created By: " + version.getCreateBy()));
            document.add(new Paragraph(String.format("Creation Datetime: " + version.getCreation_datetime(), DateTimeFormatter.ISO_DATE_TIME)));
            document.close();
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        return outputStream;
    }
}
