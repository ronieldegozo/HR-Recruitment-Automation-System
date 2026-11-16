package org.hr.recruitmen.automation.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.hr.recruitmen.automation.repository.jpa.JobApplicationReadRepository;

@ApplicationScoped
public class JobApplicationReadService {

    @Inject
    JobApplicationReadRepository repository;

    @Transactional(Transactional.TxType.SUPPORTS)
    public long countApplications() {
        return repository.countApplications();
    }
}
