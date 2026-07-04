package jobs.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ExternalJob implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Job title is required")
    private String title;

    @NotBlank(message = "Company is required")
    private String company;

    private String location;

    @NotBlank(message = "Job description is required")
    private String description;

    private String employmentType;

    @NotBlank(message = "Apply URL is required")
    private String applyUrl;

    private LocalDateTime publishedAt;

    private String provider;

    private String providerJobId;
}
