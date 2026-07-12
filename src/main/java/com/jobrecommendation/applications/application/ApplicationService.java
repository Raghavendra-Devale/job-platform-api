package com.jobrecommendation.applications.application;

import com.jobrecommendation.applications.domain.JobApplicationEntity;
import com.jobrecommendation.applications.domain.SavedJobEntity;
import com.jobrecommendation.applications.domain.repository.JobApplicationRepository;
import com.jobrecommendation.applications.domain.repository.SavedJobRepository;
import com.jobrecommendation.jobs.domain.Job;
import com.jobrecommendation.jobs.domain.JobEntity;
import com.jobrecommendation.user.domain.UserEntity;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final SavedJobRepository savedJobRepository;
    private final JobApplicationRepository jobApplicationRepository;

    // Saved Jobs
    public boolean existsSavedJob(UserEntity user, JobEntity job) {
        return savedJobRepository.existsByUserAndJob(user, job);
    }

    public SavedJobEntity saveSavedJob(SavedJobEntity savedJob) {

        return savedJobRepository.save(savedJob);
    }

    public void deleteSavedJob(UserEntity user, JobEntity job) {
        savedJobRepository.findByUserAndJob(user, job)
                .ifPresent(savedJobRepository::delete);
    }

    public List<SavedJobEntity> getSavedJobsForUser(UserEntity user) {
        return savedJobRepository.findByUserOrderBySavedAtDesc(user);
    }

    public long getSavedJobsCountForUser(UserEntity user) {

        return savedJobRepository.countByUser(user);
    }

    // Job Applications
    public long getJobApplicationsCountForUser(UserEntity user) {

        return jobApplicationRepository.countByUser(user);
    }

    public long getJobApplicationsCountForUserAndStatus(UserEntity user, String status) {
        return jobApplicationRepository.countByUserAndStatus(user, status);
    }

    public List<JobApplicationEntity> getJobApplicationsForUser(UserEntity user) {
        return jobApplicationRepository.findByUserOrderByAppliedAtDesc(user);
    }

    public Optional<JobApplicationEntity> findJobApplicationByUserAndJob(UserEntity user, JobEntity job) {
        return jobApplicationRepository.findByUserAndJob(user, job);
    }

    public JobApplicationEntity saveJobApplication(JobApplicationEntity application) {
        return jobApplicationRepository.save(application);
    }

    public Optional<JobApplicationEntity> findJobApplicationById(Long id) {
        return jobApplicationRepository.findById(id);
    }

    public void deleteJobApplication(JobApplicationEntity application) {

        jobApplicationRepository.delete(application);
    }
}
