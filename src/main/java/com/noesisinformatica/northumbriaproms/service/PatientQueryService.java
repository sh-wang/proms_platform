package com.noesisinformatica.northumbriaproms.service;

/*-
 * #%L
 * Proms Platform
 * %%
 * Copyright (C) 2017 - 2018 Termlex
 * %%
 * This software is Copyright and Intellectual Property of Termlex Inc Limited.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation as version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with this program.  If not, see
 * <https://www.gnu.org/licenses/agpl-3.0.en.html>.
 * #L%
 */

import com.noesisinformatica.northumbriaproms.domain.Address_;
import com.noesisinformatica.northumbriaproms.domain.Patient;
import com.noesisinformatica.northumbriaproms.domain.Patient_;
import com.noesisinformatica.northumbriaproms.domain.ProcedureBooking_;
import com.noesisinformatica.northumbriaproms.repository.PatientRepository;
import com.noesisinformatica.northumbriaproms.repository.search.PatientSearchRepository;
import com.noesisinformatica.northumbriaproms.service.dto.PatientCriteria;
import io.github.jhipster.service.QueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for executing complex queries for Patient entities in the database.
 * The main input is a {@link PatientCriteria} which get's converted to {@link Specifications},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link Patient} or a {@link Page} of {@link Patient} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class PatientQueryService extends QueryService<Patient> {

    private final Logger log = LoggerFactory.getLogger(PatientQueryService.class);


    private final PatientRepository patientRepository;

    private final PatientSearchRepository patientSearchRepository;

    public PatientQueryService(PatientRepository patientRepository, PatientSearchRepository patientSearchRepository) {
        this.patientRepository = patientRepository;
        this.patientSearchRepository = patientSearchRepository;
    }

    /**
     * Return a {@link List} of {@link Patient} which matches the criteria from the database
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<Patient> findByCriteria(PatientCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specifications<Patient> specification = createSpecification(criteria);
        return patientRepository.findAll(specification);
    }

    /**
     * Return a {@link Page} of {@link Patient} which matches the criteria from the database
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<Patient> findByCriteria(PatientCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specifications<Patient> specification = createSpecification(criteria);
        return patientRepository.findAll(specification, page);
    }

    /**
     * Function to convert PatientCriteria to a {@link Specifications}
     */
    private Specifications<Patient> createSpecification(PatientCriteria criteria) {
        Specifications<Patient> specification = Specifications.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildSpecification(criteria.getId(), Patient_.id));
            }
            if (criteria.getFamilyName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getFamilyName(), Patient_.familyName));
            }
            if (criteria.getGivenName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getGivenName(), Patient_.givenName));
            }
            if (criteria.getBirthDate() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getBirthDate(), Patient_.birthDate));
            }
            if (criteria.getGender() != null) {
                specification = specification.and(buildSpecification(criteria.getGender(), Patient_.gender));
            }
            if (criteria.getNhsNumber() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getNhsNumber(), Patient_.nhsNumber));
            }
            if (criteria.getEmail() != null) {
                specification = specification.and(buildStringSpecification(criteria.getEmail(), Patient_.email));
            }
            if (criteria.getAddressId() != null) {
                specification = specification.and(buildReferringEntitySpecification(criteria.getAddressId(), Patient_.addresses, Address_.id));
            }
            if (criteria.getProcedureBookingsId() != null) {
                specification = specification.and(buildReferringEntitySpecification(criteria.getProcedureBookingsId(), Patient_.procedureBookings, ProcedureBooking_.id));
            }
        }
        return specification;
    }

}
