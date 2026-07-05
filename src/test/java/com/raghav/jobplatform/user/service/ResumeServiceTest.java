// package com.raghav.jobplatform.user.service;

// import com.raghav.jobplatform.common.ai.AIClient;
// import com.raghav.jobplatform.common.ai.AIException;
// import com.raghav.jobplatform.common.ai.dto.ResumeIntelligenceResponse;
// import com.raghav.jobplatform.common.ai.dto.ResumeProcessRequest;
// import com.raghav.jobplatform.user.entity.*;
// import com.raghav.jobplatform.user.repository.*;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.mock.web.MockMultipartFile;

// import java.io.IOException;
// import java.util.List;
// import java.util.Optional;

// import static org.assertj.core.api.Assertions.assertThat;
// import static org.assertj.core.api.Assertions.assertThatThrownBy;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class)
// class ResumeServiceTest {

// @Mock
// private ResumeRepository resumeRepository;

// @Mock
// private UserRepository userRepository;

// @Mock
// private ActivityService activityService;

// @Mock
// private AIClient aiClient;

// @Mock
// private ResumeSummaryRepository resumeSummaryRepository;

// @Mock
// private ResumeSkillRepository resumeSkillRepository;

// @Mock
// private ResumeEducationRepository resumeEducationRepository;

// @Mock
// private ResumeExperienceRepository resumeExperienceRepository;

// @Mock
// private ResumeProjectRepository resumeProjectRepository;

// @InjectMocks
// private ResumeService resumeService;

// private UserEntity user;

// @BeforeEach
// void setUp() {
// resumeService.setSelf(resumeService); // Setup self-invocation reference

// user = UserEntity.builder()
// .id(1L)
// .email("test@example.com")
// .name("Test User")
// .build();
// }

// @Test
// void uploadResume_SuccessWithAiProcessing() throws IOException {
// // Arrange
// MockMultipartFile file = new MockMultipartFile(
// "file",
// "resume.pdf",
// "application/pdf",
// "pdf content".getBytes()
// );

// ResumeEntity savedResume = ResumeEntity.builder()
// .id(100L)
// .user(user)
// .resumeName("resume.pdf")
// .resumeData(file.getBytes())
// .isActive(true)
// .aiProcessingStatus(AiProcessingStatus.PROCESSING)
// .build();

// ResumeIntelligenceResponse aiResponse = new ResumeIntelligenceResponse();
// aiResponse.setSuccess(true);
// aiResponse.setProcessingTimeMs(150.0);

// ResumeIntelligenceResponse.ResumeDetails details = new
// ResumeIntelligenceResponse.ResumeDetails();
// details.setSummary("Experienced Software Engineer.");
// details.setSkills(List.of(new ResumeIntelligenceResponse.SkillDto("Java",
// 0.95)));
// details.setEducation(List.of(new
// ResumeIntelligenceResponse.EducationDto("B.S.", "MIT", "2018", "2022")));
// details.setExperience(List.of(new
// ResumeIntelligenceResponse.ExperienceDto("Google", "SWE", "2022", "Present",
// List.of("Coding"))));
// details.setProjects(List.of(new ResumeIntelligenceResponse.ProjectDto("AI
// Engine", "FastAPI", List.of("Python"))));
// aiResponse.setResume(details);

// when(resumeRepository.countByUser(user)).thenReturn(1L);
// when(resumeRepository.save(any(ResumeEntity.class))).thenReturn(savedResume);
// when(resumeRepository.findById(100L)).thenReturn(Optional.of(savedResume));
// when(aiClient.processResume(any(ResumeProcessRequest.class))).thenReturn(aiResponse);

// // Act
// ResumeEntity result = resumeService.uploadResume(user, file);

// // Assert
// assertThat(result).isNotNull();
// assertThat(result.getId()).isEqualTo(100L);
// verify(resumeRepository, atLeastOnce()).save(any(ResumeEntity.class));
// verify(resumeSummaryRepository).save(any(ResumeSummaryEntity.class));
// verify(resumeSkillRepository).save(any(ResumeSkillEntity.class));
// verify(resumeEducationRepository).save(any(ResumeEducationEntity.class));
// verify(resumeExperienceRepository).save(any(ResumeExperienceEntity.class));
// verify(resumeProjectRepository).save(any(ResumeProjectEntity.class));
// verify(activityService).log(eq(user), eq("RESUME_UPLOADED"), anyString());
// }

// @Test
// void uploadResume_SuccessEvenIfAiProcessingFails() throws IOException {
// // Arrange
// MockMultipartFile file = new MockMultipartFile(
// "file",
// "resume.pdf",
// "application/pdf",
// "pdf content".getBytes()
// );

// ResumeEntity savedResume = ResumeEntity.builder()
// .id(100L)
// .user(user)
// .resumeName("resume.pdf")
// .resumeData(file.getBytes())
// .isActive(true)
// .aiProcessingStatus(AiProcessingStatus.PROCESSING)
// .build();

// when(resumeRepository.countByUser(user)).thenReturn(1L);
// when(resumeRepository.save(any(ResumeEntity.class))).thenReturn(savedResume);
// when(resumeRepository.findById(100L)).thenReturn(Optional.of(savedResume));

// // Mock AI failure
// when(aiClient.processResume(any(ResumeProcessRequest.class))).thenThrow(new
// AIException("AI engine timeout"));

// // Act
// ResumeEntity result = resumeService.uploadResume(user, file);

// // Assert
// assertThat(result).isNotNull();
// assertThat(result.getId()).isEqualTo(100L);

// // Confirm AI status gets set to FAILED and saved
// verify(resumeRepository, atLeastOnce()).save(argThat(r ->
// r.getAiProcessingStatus() == AiProcessingStatus.FAILED));

// // Confirm no child entities are saved since AI failed
// verifyNoInteractions(resumeSummaryRepository);
// verifyNoInteractions(resumeSkillRepository);
// }

// @Test
// void uploadResume_RejectsEmptyFile() {
// MockMultipartFile file = new MockMultipartFile(
// "file",
// "resume.pdf",
// "application/pdf",
// new byte[0]
// );

// assertThatThrownBy(() -> resumeService.uploadResume(user, file))
// .isInstanceOf(IllegalArgumentException.class)
// .hasMessageContaining("File is empty");
// }

// @Test
// void uploadResume_RejectsOversizedFile() {
// byte[] oversizedBytes = new byte[6 * 1024 * 1024]; // 6MB
// MockMultipartFile file = new MockMultipartFile(
// "file",
// "resume.pdf",
// "application/pdf",
// oversizedBytes
// );

// assertThatThrownBy(() -> resumeService.uploadResume(user, file))
// .isInstanceOf(IllegalArgumentException.class)
// .hasMessageContaining("File size exceeds limit");
// }

// @Test
// void uploadResume_RejectsCountExceeded() {
// MockMultipartFile file = new MockMultipartFile(
// "file",
// "resume.pdf",
// "application/pdf",
// "pdf content".getBytes()
// );

// when(resumeRepository.countByUser(user)).thenReturn(4L);

// assertThatThrownBy(() -> resumeService.uploadResume(user, file))
// .isInstanceOf(IllegalArgumentException.class)
// .hasMessageContaining("maximum of 4 resumes");
// }

// @Test
// void uploadResume_RejectsUnsupportedExtension() {
// MockMultipartFile file = new MockMultipartFile(
// "file",
// "resume.png",
// "image/png",
// "png content".getBytes()
// );

// assertThatThrownBy(() -> resumeService.uploadResume(user, file))
// .isInstanceOf(IllegalArgumentException.class)
// .hasMessageContaining("Only PDF and Word documents");
// }
// }
