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

import com.noesisinformatica.northumbriaproms.domain.CareEvent;
import com.noesisinformatica.northumbriaproms.domain.FollowupAction;
import com.noesisinformatica.northumbriaproms.web.rest.util.QueryModel;
import com.noesisinformatica.northumbriaproms.web.rest.util.QuestionnaireQueryModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.FacetedPage;

/**
 * Service Interface for managing FollowupAction.
 */
public interface FollowupActionService {

    /**
     * Save a followupAction.
     *
     * @param followupAction the entity to save
     * @return the persisted entity
     */
    FollowupAction save(FollowupAction followupAction);

    /**
     * Utility method that handles processing of a {@link FollowupAction}
     * @param followupAction the action to process
     */
    void processFollowupAction(FollowupAction followupAction);

    /**
     * Process a {@link com.noesisinformatica.northumbriaproms.domain.CareEvent}.
     *
     * @param careEvent the entity to process
     */
    void processCareEvent(CareEvent careEvent);

    /**
     * Get all the followupActions.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<FollowupAction> findAll(Pageable pageable);

    /**
     * Get the "id" followupAction.
     *
     * @param id the id of the entity
     * @return the entity
     */
    FollowupAction findOne(Long id);

    /**
     * Delete the "id" followupAction.
     *
     * @param id the id of the entity
     */
    void delete(Long id);

    /**
     * Return all followup actions with all categories.
     *
     *  @return the page of followup actions
     */
    FacetedPage<FollowupAction> findAllWithCategories(Pageable pageable);

    /**
     *  Index all the followup actions.
     *
     *  @return the boolean that represents the success of the index action
     */
    boolean indexAll();

    /**
     * Search for the followupAction corresponding to the query.
     *
     * @param query the query of the search
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    FacetedPage<FollowupAction> search(QueryModel query, Pageable pageable);

    /**
     * Search for the followupAction corresponding to the query.
     *
     * @param query the query of the search
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    FacetedPage<FollowupAction> searchQuestionnaire(QuestionnaireQueryModel query, Pageable pageable);

    /**
     * Search for the FollowupAction corresponding to the query.
     *
     * @param query the query of the search
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<FollowupAction> search(String query, Pageable pageable);
}
