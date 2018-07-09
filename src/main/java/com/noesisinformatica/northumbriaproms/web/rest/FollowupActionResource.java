package com.noesisinformatica.northumbriaproms.web.rest;

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

import com.codahale.metrics.annotation.Timed;
import com.noesisinformatica.northumbriaproms.domain.FollowupAction;
import com.noesisinformatica.northumbriaproms.service.FollowupActionQueryService;
import com.noesisinformatica.northumbriaproms.service.FollowupActionService;
import com.noesisinformatica.northumbriaproms.service.dto.FollowupActionCriteria;
import com.noesisinformatica.northumbriaproms.web.rest.errors.BadRequestAlertException;
import com.noesisinformatica.northumbriaproms.web.rest.util.HeaderUtil;
import com.noesisinformatica.northumbriaproms.web.rest.util.PaginationUtil;
import com.noesisinformatica.northumbriaproms.web.rest.util.QueryModel;
import com.opencsv.CSVWriter;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import io.github.jhipster.service.filter.LongFilter;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.FacetedPage;
import org.springframework.data.elasticsearch.core.facet.result.Term;
import org.springframework.data.elasticsearch.core.facet.result.TermResult;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * REST controller for managing FollowupAction.
 */
@RestController
@RequestMapping("/api")
public class FollowupActionResource {

    private final Logger log = LoggerFactory.getLogger(FollowupActionResource.class);

    private static final String ENTITY_NAME = "followupAction";

    private final FollowupActionService followupActionService;

    private final FollowupActionQueryService followupActionQueryService;

    public FollowupActionResource(FollowupActionService followupActionService, FollowupActionQueryService followupActionQueryService) {
        this.followupActionService = followupActionService;
        this.followupActionQueryService = followupActionQueryService;
    }

    /**
     * POST  /followup-actions : Create a new followupAction.
     *
     * @param followupAction the followupAction to create
     * @return the ResponseEntity with status 201 (Created) and with body the new followupAction, or with status 400 (Bad Request) if the followupAction has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/followup-actions")
    @Timed
    public ResponseEntity<FollowupAction> createFollowupAction(@Valid @RequestBody FollowupAction followupAction) throws URISyntaxException {
        log.debug("REST request to save FollowupAction : {}", followupAction);
        if (followupAction.getId() != null) {
            throw new BadRequestAlertException("A new followupAction cannot already have an ID", ENTITY_NAME, "idexists");
        }
        FollowupAction result = followupActionService.save(followupAction);
        return ResponseEntity.created(new URI("/api/followup-actions/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /followup-actions : Updates an existing followupAction.
     *
     * @param followupAction the followupAction to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated followupAction,
     * or with status 400 (Bad Request) if the followupAction is not valid,
     * or with status 500 (Internal Server Error) if the followupAction couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/followup-actions")
    @Timed
    public ResponseEntity<FollowupAction> updateFollowupAction(@Valid @RequestBody FollowupAction followupAction) throws URISyntaxException {
        log.debug("REST request to update FollowupAction : {}", followupAction);
        if (followupAction.getId() == null) {
            return createFollowupAction(followupAction);
        }
        FollowupAction result = followupActionService.save(followupAction);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, followupAction.getId().toString()))
            .body(result);
    }

    /**
     * GET  /followup-actions : get all the followupActions.
     *
     * @param pageable the pagination information
     * @param criteria the criterias which the requested entities should match
     * @return the ResponseEntity with status 200 (OK) and the list of followupActions in body
     */
    @GetMapping("/followup-actions")
    @Timed
    public ResponseEntity<List<FollowupAction>> getAllFollowupActions(FollowupActionCriteria criteria, Pageable pageable) {
        log.debug("REST request to get FollowupActions by criteria: {}", criteria);
        Page<FollowupAction> page = followupActionQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/followup-actions");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /patient/:id/followup-actions : get all the followupActions for patient id
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of followupActions in body
     */
    @GetMapping("/patient/{id}/followup-actions")
    @Timed
    public ResponseEntity<List<FollowupAction>> getAllFollowupActions(@PathVariable Long id, Pageable pageable) {
        log.debug("REST request to get FollowupActions for patient id: {}", id);
        FollowupActionCriteria criteria = new FollowupActionCriteria();
        LongFilter filter = new LongFilter();
        filter.setEquals(id);
        criteria.setPatientId(filter);
        Page<FollowupAction> page = followupActionQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/patient/" + id + "/followup-actions");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /care-events/:id/followup-actions : get all the followup actions for care event id
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of follow up actions in body
     */
    @GetMapping("/care-events/{id}/followup-actions")
    @Timed
    public ResponseEntity<List<FollowupAction>> getAllFollowupActionsForCareEvent(@PathVariable Long id, Pageable pageable) {
        log.debug("REST request to get care-events for followup-plan id: {}", id);
        FollowupActionCriteria criteria = new FollowupActionCriteria();
        LongFilter filter = new LongFilter();
        filter.setEquals(id);
        criteria.setCareEventId(filter);
        Page<FollowupAction> page = followupActionQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/care-events/" + id + "/followup-actions");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /followup-actions/:id : get the "id" followupAction.
     *
     * @param id the id of the followupAction to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the followupAction, or with status 404 (Not Found)
     */
    @GetMapping("/followup-actions/{id}")
    @Timed
    public ResponseEntity<FollowupAction> getFollowupAction(@PathVariable Long id) {
        log.debug("REST request to get FollowupAction : {}", id);
        FollowupAction followupAction = followupActionService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(followupAction));
    }

    /**
     * DELETE  /followup-actions/:id : delete the "id" followupAction.
     *
     * @param id the id of the followupAction to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/followup-actions/{id}")
    @Timed
    public ResponseEntity<Void> deleteFollowupAction(@PathVariable Long id) {
        log.debug("REST request to delete FollowupAction : {}", id);
        followupActionService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    /**
     * SEARCH  /_search/followup-actions?query=:query : search for the followupAction corresponding
     * to the query.
     *
     * @param query the query of the followupAction search
     * @param pageable the pagination information
     * @return the result of the search
     */
    @PostMapping("/_search/followup-actions")
    @Timed
    public ResponseEntity<Map<String, Object>> searchFollowupActions(@RequestBody QueryModel query, Pageable pageable) {
        log.debug("REST request to search for a page of FollowupActions for query {}", query);
        FacetedPage<FollowupAction> page = followupActionService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query.toString(), page, "/api/_search/followup-actions");
        // wrap results page in a response entity with faceted results turned into a map
        return new ResponseEntity<>(getResultMapMapForResults(page), headers, HttpStatus.OK);
    }

    /**
     * Download  /_export/followup-actions : export the followup action corresponding to the query.
     *
     * @param query the query of the followupAction search
     * @param pageable the pagination information
     * @return the result of the search
     */
    @PostMapping("/_export/followup-actions")
    @Timed
    public void exportFollowupActions(@RequestBody QueryModel query, Pageable pageable,
                                                                     HttpServletResponse response) throws IOException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
        log.debug("REST request to search for a page of FollowupActions for query {}", query);
        FacetedPage<FollowupAction> page = followupActionService.search(query, pageable);
        response.setContentType("text/csv");
        response.setHeader("content-disposition","attachment;filename=export.csv");
        // wrap results page in a response entity with faceted results turned into a map
        ColumnPositionMappingStrategy mapStrategy = new ColumnPositionMappingStrategy();
        mapStrategy.setType(FollowupAction.class);

        String[] columns = new String[]{"id","last_name", "first_name", "nhs_number", "gender", "age",
            "consultant_name", "hospital_name", "primary_procedure", "performed_date",
            "name", "time_point", "outcomeScore", "completed_date",  "phase", "outcome_comment",
            };
        mapStrategy.setColumnMapping(columns);

        OutputStreamWriter osw = new OutputStreamWriter(response.getOutputStream(), "UTF-8");
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        CSVWriter csvWriter = new CSVWriter(osw);
        csvWriter.writeNext(columns);
        page.getContent().forEach(action -> {
            List<String> row = new ArrayList<>();
            row.add(action.getId().toString());
            row.add(action.getCareEvent().getFollowupPlan().getProcedureBooking().getPatient().getFamilyName());
            row.add(action.getCareEvent().getFollowupPlan().getProcedureBooking().getPatient().getGivenName());
            row.add(action.getCareEvent().getFollowupPlan().getProcedureBooking().getPatient().getNhsNumber().toString());
            row.add(action.getCareEvent().getFollowupPlan().getProcedureBooking().getPatient().getGender().name());
            row.add(action.getCareEvent().getFollowupPlan().getProcedureBooking().getPatientAge().toString());
            row.add(action.getCareEvent().getFollowupPlan().getProcedureBooking().getConsultantName());
            row.add(action.getCareEvent().getFollowupPlan().getProcedureBooking().getHospitalSite());
            row.add(action.getCareEvent().getFollowupPlan().getProcedureBooking().getPrimaryProcedure());
            row.add(action.getCareEvent().getFollowupPlan().getProcedureBooking().getPerformedDate().format(dateFormat));
            row.add(action.getName());
            row.add(action.getCareEvent().getTimepoint().getName());
            row.add(action.getOutcomeScore().toString());
            if (action.getCompletedDate() != null) {
                row.add(action.getCompletedDate().format(dateFormat));
            } else {
                row.add("");
            }
            row.add(action.getPhase().name());
            row.add(action.getOutcomeComment());
            // write to csv
            csvWriter.writeNext(row.toArray(new String[columns.length]));
        });

        csvWriter.close();
        osw.close();
        response.flushBuffer();
    }

    /**
     * Utility private method for transforming a {@link FacetedPage} into a {@link Map} object with results
     * and categories.
     * @param page the page of results
     * @return results as a Map
     */
    private Map<String, Object> getResultMapMapForResults(FacetedPage<FollowupAction> page) {

        Set<String> items = new HashSet<>();
        items.add("types");
        items.add("procedures");
        items.add("phases");
        items.add("locations");
        items.add("genders");
        items.add("consultants");
        Map<String, Set<Map<String, Object>>> facetsMap = new HashMap<>();
        for (String key : items) {
            log.info("Processed facet key : {}", key);
            // get search categories via facets
            Set<Map<String, Object>> mapSet = new HashSet<>();
            TermResult pageFacet = (TermResult) page.getFacet(key);

            if (pageFacet != null) {
                for (Term bucket : pageFacet.getTerms()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("type", bucket.getTerm());
                    map.put("count", bucket.getCount());
                    mapSet.add(map);
                }
            }

            // add to facets map
            facetsMap.put(key, mapSet);
        }
        log.debug("facetsMap {}", facetsMap);
        Map<String, Object> resultsMap = new HashMap<>();
        resultsMap.put("results", page.getContent());
        resultsMap.put("categories", facetsMap);

        return resultsMap;
    }

    /**
     * INDEX  /_index/followup-actions : index all followup-actions
     *
     * @return the result of the index action
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/_index/followup-actions")
    @Timed
    public ResponseEntity<Boolean> indexFollowupActions() throws URISyntaxException {
        log.debug("REST request to bulk index all followup actions");
        Boolean result = followupActionService.indexAll();
        if (result) {
            return ResponseEntity.ok().body(true);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    public Map<String, Object> getMapResults(FacetedPage<FollowupAction> page){
        return getResultMapMapForResults(page);
    }

}
