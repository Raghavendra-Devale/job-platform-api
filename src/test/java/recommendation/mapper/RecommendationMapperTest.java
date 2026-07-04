package recommendation.mapper;

import ai.dto.JobDocument;
import ai.dto.RecommendationRequest;
import com.raghav.jobplatform.jobs.model.Job;
import com.raghav.jobplatform.user.entity.ResumeEntity;
import com.raghav.jobplatform.user.entity.UserEntity;
import org.junit.jupiter.api.Test;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RecommendationMapperTest {

    private final RecommendationMapper mapper = new RecommendationMapper();

    @Test
    void toRequest_ResumeMapping() {
        // Arrange
        UserEntity user = mock(UserEntity.class);
        when(user.getName()).thenReturn("John Doe");

        ResumeEntity resume = mock(ResumeEntity.class);
        when(resume.getUser()).thenReturn(user);

        // Act
        RecommendationRequest request = mapper.toRequest(resume, Collections.emptyList());

        // Assert
        assertThat(request.getResumeText()).contains("Resume of John Doe");
    }

    @Test
    void toRequest_JobsMapping() {
        // Arrange
        UserEntity user = mock(UserEntity.class);
        when(user.getName()).thenReturn("John Doe");

        ResumeEntity resume = mock(ResumeEntity.class);
        when(resume.getUser()).thenReturn(user);

        Job job = new Job(
                "Software Engineer",
                "Google",
                "Mountain View",
                "Write Java code",
                "http://google.com/apply",
                true,
                "$150k",
                "Full-time",
                "LinkedIn"
        );

        // Act
        RecommendationRequest request = mapper.toRequest(resume, List.of(job));

        // Assert
        assertThat(request.getJobs()).hasSize(1);
        JobDocument doc = request.getJobs().get(0);
        assertThat(doc.getTitle()).isEqualTo("Software Engineer");
        assertThat(doc.getCompany()).isEqualTo("Google");
        assertThat(doc.getLocation()).isEqualTo("Mountain View");
        assertThat(doc.getDescription()).isEqualTo("Write Java code");
        assertThat(doc.getApplyUrl()).isEqualTo("http://google.com/apply");
        assertThat(doc.getEmploymentType()).isEqualTo("Full-time");
    }

    @Test
    void toRequest_EmptyJobs() {
        // Arrange
        UserEntity user = mock(UserEntity.class);
        when(user.getName()).thenReturn("John Doe");

        ResumeEntity resume = mock(ResumeEntity.class);
        when(resume.getUser()).thenReturn(user);

        // Act
        RecommendationRequest request = mapper.toRequest(resume, Collections.emptyList());

        // Assert
        assertThat(request.getJobs()).isEmpty();
    }

    @Test
    void toRequestFromDocuments_Success() {
        // Arrange
        UserEntity user = mock(UserEntity.class);
        when(user.getName()).thenReturn("John Doe");

        ResumeEntity resume = mock(ResumeEntity.class);
        when(resume.getUser()).thenReturn(user);

        JobDocument doc = JobDocument.builder()
                .title("Software Engineer")
                .company("Google")
                .location("Mountain View")
                .description("Write Java code")
                .applyUrl("http://google.com/apply")
                .employmentType("Full-time")
                .build();

        // Act
        RecommendationRequest request = mapper.toRequestFromDocuments(resume, List.of(doc));

        // Assert
        assertThat(request.getResumeText()).contains("Resume of John Doe");
        assertThat(request.getJobs()).hasSize(1);
        assertThat(request.getJobs().get(0)).isSameAs(doc);
    }
}
