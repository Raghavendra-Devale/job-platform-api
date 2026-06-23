package com.raghav.jobplatform.jobs.specification;

import com.raghav.jobplatform.jobs.entity.JobEntity;
import org.springframework.data.jpa.domain.Specification;

public class JobSpecification {

    public static Specification<JobEntity> hasKeyword(
            String keyword
    ) {

        return (root, query, cb) -> {

            if (keyword == null || keyword.isBlank()) {
                return cb.conjunction();
            }

            return cb.like(
                    cb.lower(root.get("title")),
                    "%" + keyword.toLowerCase() + "%"
            );
        };
    }

    public static Specification<JobEntity> hasLocation(
            String location
    ) {

        return (root, query, cb) -> {

            if (location == null || location.isBlank()) {
                return cb.conjunction();
            }

            return cb.like(
                    cb.lower(root.get("location")),
                    "%" + location.toLowerCase() + "%"
            );
        };
    }

    public static Specification<JobEntity> hasRemote(
            Boolean remote
    ) {

        return (root, query, cb) -> {

            if (remote == null) {
                return cb.conjunction();
            }

            return cb.equal(
                    root.get("remote"),
                    remote
            );
        };
    }
}