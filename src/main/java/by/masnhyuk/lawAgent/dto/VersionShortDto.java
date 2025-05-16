package by.masnhyuk.lawAgent.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VersionShortDto {
    private UUID id;
    private LocalDateTime createdAt;
    private String sourceUrl;
    private String details;
    private Integer number;
}
