package br.com.hinova.powerpdf.service;

import br.com.hinova.powerpdf.dto.PdfMergeResponseDto;
import br.com.hinova.powerpdf.model.MergedPdf;
import br.com.hinova.powerpdf.repository.MergedPdfRepository;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.*;
import java.util.stream.Collectors;

import static br.com.hinova.powerpdf.configuration.RabbitMQConfig.PDF_MERGE_EXCHANGE;
import static br.com.hinova.powerpdf.configuration.RabbitMQConfig.PDF_MERGE_ROUTING_KEY;

@Service
public class PdfMergeService {

    private final MergedPdfRepository mergedPdfRepository;
    private final RabbitTemplate rabbitTemplate;

    @Value("${file.storage.location}")
    private String fileStorageLocation;

    public PdfMergeService(MergedPdfRepository mergedPdfRepository, RabbitTemplate rabbitTemplate) {
        this.mergedPdfRepository = mergedPdfRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public PdfMergeResponseDto mergePdfs(String name, MultipartFile[] files) throws Exception {
        // Save uploaded files to temporary location
        File[] pdfFiles = new File[files.length];
        for (int i = 0; i < files.length; i++) {
            File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + files[i].getOriginalFilename());
            FileOutputStream fos = new FileOutputStream(convFile);
            fos.write(files[i].getBytes());
            fos.close();
            pdfFiles[i] = convFile;
        }

        // Generate a unique file name for the merged PDF
        String mergedFileName = UUID.randomUUID().toString() + ".pdf";
        String mergedFilePath = fileStorageLocation + "/" + mergedFileName;

        // Create a PDFMergerUtility instance
        PDFMergerUtility pdfMerger = new PDFMergerUtility();
        pdfMerger.setDestinationFileName(mergedFilePath);

        // Add source PDFs
        for (File pdfFile : pdfFiles) {
            pdfMerger.addSource(pdfFile);
        }

        // Perform the merge (this can be offloaded to a message queue)
        pdfMerger.mergeDocuments(null);


        // Save record to database
        MergedPdf mergedPdf = new MergedPdf();
        mergedPdf.setName(name);
        mergedPdf.setFilePath(mergedFilePath);
        mergedPdfRepository.save(mergedPdf);

        // Create response DTO
        PdfMergeResponseDto responseDto = new PdfMergeResponseDto();
        responseDto.setId(mergedPdf.getId());
        responseDto.setName(mergedPdf.getName());
        responseDto.setLink("/merged-pdfs/" + mergedPdf.getId());
        responseDto.setCreatedAt(mergedPdf.getCreatedAt());

        // Prepare message payload

        return responseDto;
    }

    public List<MergedPdf> getAllMergedPdfs() {
        return mergedPdfRepository.findAll();
    }

    public PdfMergeResponseDto convertToDto(MergedPdf mergedPdf) {
        PdfMergeResponseDto dto = new PdfMergeResponseDto();
        dto.setId(mergedPdf.getId());
        dto.setName(mergedPdf.getName());
        dto.setLink("/pdfs/merged/" + mergedPdf.getId());
        dto.setCreatedAt(mergedPdf.getCreatedAt());
        return dto;
    }

    public File getMergedPdfFile(Long id) throws FileNotFoundException {
        MergedPdf mergedPdf = mergedPdfRepository.findById(id)
                .orElseThrow(() -> new FileNotFoundException("Merged PDF not found"));

        File file = new File(mergedPdf.getFilePath());
        if (!file.exists()) {
            throw new FileNotFoundException("File not found on disk");
        }
        return file;
    }


}
