package br.com.hinova.powerpdf.service;

import br.com.hinova.powerpdf.model.MergedPdf;
import br.com.hinova.powerpdf.repository.MergedPdfRepository;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.Map;

import static br.com.hinova.powerpdf.configuration.RabbitMQConfig.PDF_MERGE_QUEUE;

@Component
public class PdfMergeListener {

    private final MergedPdfRepository mergedPdfRepository;

    public PdfMergeListener(MergedPdfRepository mergedPdfRepository) {
        this.mergedPdfRepository = mergedPdfRepository;
    }

    @RabbitListener(queues = PDF_MERGE_QUEUE)
    public void handlePdfMerge(Map<String, Object> message) throws Exception {
        Long mergedPdfId = (Long) message.get("mergedPdfId");
        List<String> filePaths = (List<String>) message.get("filePaths");
        String mergedFilePath = (String) message.get("mergedFilePath");

        // Perform the merge
        PDFMergerUtility pdfMerger = new PDFMergerUtility();
        pdfMerger.setDestinationFileName(mergedFilePath);

        for (String path : filePaths) {
            pdfMerger.addSource(new File(path));
        }

        pdfMerger.mergeDocuments(null);

        // Update the database record if needed
        MergedPdf mergedPdf = mergedPdfRepository.findById(mergedPdfId).orElseThrow();
        mergedPdfRepository.save(mergedPdf);

        // Clean up temporary files
        for (String path : filePaths) {
            new File(path).delete();
        }
    }
}
