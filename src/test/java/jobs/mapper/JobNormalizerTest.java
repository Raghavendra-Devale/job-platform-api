package jobs.mapper;

import ai.dto.JobDocument;
import jobs.dto.ExternalJob;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class JobNormalizerTest {

    private final JobNormalizer normalizer = new JobNormalizer();

    @Test
    void normalize_FieldMappingAndFormatting() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        ExternalJob externalJob = ExternalJob.builder()
                .title("   Senior Java Developer   ")
                .company("  Acme Corp  ")
                .location("  San Francisco, CA  ")
                .description("  <p>We are looking for a <b>Java Dev</b>.</p>  ")
                .employmentType("  Contract  ")
                .applyUrl("  https://acme.com/apply  ")
                .publishedAt(now)
                .build();

        // Act
        JobDocument document = normalizer.normalize(externalJob);

        // Assert
        assertThat(document).isNotNull();
        assertThat(document.getTitle()).isEqualTo("Senior Java Developer");
        assertThat(document.getCompany()).isEqualTo("Acme Corp");
        assertThat(document.getLocation()).isEqualTo("San Francisco, CA");
        assertThat(document.getDescription()).isEqualTo("We are looking for a Java Dev.");
        assertThat(document.getEmploymentType()).isEqualTo("Contract");
        assertThat(document.getApplyUrl()).isEqualTo("https://acme.com/apply");
        assertThat(document.getPublishedAt()).isEqualTo(now);
    }

    @Test
    void normalize_NullValues() {
        // Arrange
        ExternalJob externalJob = ExternalJob.builder()
                .title(null)
                .company(null)
                .location("")
                .description(null)
                .employmentType("   ")
                .applyUrl(null)
                .publishedAt(null)
                .build();

        // Act
        JobDocument document = normalizer.normalize(externalJob);

        // Assert
        assertThat(document).isNotNull();
        assertThat(document.getTitle()).isEqualTo("Unknown Position");
        assertThat(document.getCompany()).isEqualTo("Unknown Company");
        assertThat(document.getLocation()).isEqualTo("Remote");
        assertThat(document.getDescription()).isEmpty();
        assertThat(document.getEmploymentType()).isEqualTo("Full-time");
        assertThat(document.getApplyUrl()).isEmpty();
        assertThat(document.getPublishedAt()).isNull();
    }

    @Test
    void normalize_NullInput() {
        // Act
        JobDocument document = normalizer.normalize(null);

        // Assert
        assertThat(document).isNull();
    }
}
