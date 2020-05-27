package org.udg.trackdev.spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.udg.trackdev.spring.entity.PullRequest;

@Component
public interface PullRequestRepository extends JpaRepository<PullRequest, Long> {
}
