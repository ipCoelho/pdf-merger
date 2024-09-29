package br.com.hinova.powerpdf.controller;

import br.com.hinova.powerpdf.dto.PdfMergeResponseDto;
import br.com.hinova.powerpdf.model.MergedPdf;
import br.com.hinova.powerpdf.service.PdfMergeService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/pdfs")
public class PdfMergeController {

    private final PdfMergeService pdfMergeService;

    public PdfMergeController(PdfMergeService pdfMergeService) {
        this.pdfMergeService = pdfMergeService;
    }

    @PostMapping("/merge")
    public ResponseEntity<PdfMergeResponseDto> mergePdfs(
            @RequestParam("name") String name,
            @RequestParam("files") MultipartFile[] files) throws Exception {
        PdfMergeResponseDto responseDto = pdfMergeService.mergePdfs(name, files);
        return ResponseEntity.ok(responseDto);
    }

    // Endpoint to list merged PDFs
    @GetMapping("/merged")
    public ResponseEntity<List<PdfMergeResponseDto>> listMergedPdfs() {
        List<MergedPdf> mergedPdfs = pdfMergeService.getAllMergedPdfs();
        List<PdfMergeResponseDto> responseDtos = mergedPdfs.stream()
                .map(pdfMergeService::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDtos);
    }


    @GetMapping("/merged/{id}")
    public ResponseEntity<Resource> getMergedPdf(@PathVariable Long id) throws FileNotFoundException {
        File file = pdfMergeService.getMergedPdfFile(id);
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + file.getName())
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(file.length())
                .body(resource);
    }

}
