package br.com.hinova.powerpdf.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PdfMergeResponseDto {
    private Long id;
    private String name;
    private String link;
    private LocalDateTime createdAt;
}
