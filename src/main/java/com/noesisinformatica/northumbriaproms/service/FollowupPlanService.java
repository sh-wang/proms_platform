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

import com.noesisinformatica.northumbriaproms.domain.FollowupPlan;
import com.noesisinformatica.northumbriaproms.domain.ProcedureBooking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing FollowupPlan.
 */
public interface FollowupPlanService {

    /**
     * Utility method for generating {@link FollowupPlan} from a {@link ProcedureBooking}
     *
     * @param booking the ProcedureBooking to process
     */
    void processBooking(ProcedureBooking booking);

    /**
     * Save a followupPlan.
     *
     * @param followupPlan the entity to save
     * @return the persisted entity
     */
    FollowupPlan save(FollowupPlan followupPlan);

    /**
     * Get all the followupPlans.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<FollowupPlan> findAll(Pageable pageable);
    /**
     * Get all the FollowupPlanDTO where ProcedureBooking is null.
     *
     * @return the list of entities
     */
    List<FollowupPlan> findAllWhereProcedureBookingIsNull();

    /**
     * Get the "id" followupPlan.
     *
     * @param id the id of the entity
     * @return the entity
     */
    FollowupPlan findOne(Long id);

    /**
     * Get one followupPlan by {@link ProcedureBooking} id.
     *
     * @param id the id of the procedure booking
     * @return the entity
     */
    Optional<FollowupPlan> findOneByProcedureBookingId(Long id);

    /**
     * Delete the "id" followupPlan.
     *
     * @param id the id of the entity
     */
    void delete(Long id);

    /**
     * Search for the followupPlan corresponding to the query.
     *
     * @param query the query of the search
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<FollowupPlan> search(String query, Pageable pageable);
}
