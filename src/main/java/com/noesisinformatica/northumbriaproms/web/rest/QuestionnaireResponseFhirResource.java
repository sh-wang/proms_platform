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

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.codahale.metrics.annotation.Timed;
import com.noesisinformatica.northumbriaproms.domain.*;
import com.noesisinformatica.northumbriaproms.domain.Patient;
import com.noesisinformatica.northumbriaproms.domain.Questionnaire;
import com.noesisinformatica.northumbriaproms.service.FollowupActionQueryService;
import com.noesisinformatica.northumbriaproms.service.FollowupActionService;
import com.noesisinformatica.northumbriaproms.service.dto.FollowupActionCriteria;
import com.noesisinformatica.northumbriaproms.web.rest.util.PaginationUtil;
import com.noesisinformatica.northumbriaproms.web.rest.util.QueryModel;
import io.github.jhipster.service.filter.LongFilter;
import org.hl7.fhir.dstu3.model.*;
import org.mapstruct.Mapping;
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

import java.util.*;

/**
 * REST controller for Resource QuestionnaireResponse.
 */
@RestController
@RequestMapping("/api/fhir")
public class QuestionnaireResponseFhirResource {
    private final Logger log = LoggerFactory.getLogger(QuestionnaireResponseFhirResource.class);

    private final FollowupActionQueryService followupActionQueryService;
    private final FollowupActionService followupActionService;
    private final PatientFhirResource patientFhirResource;
    private final FollowupActionResource followupActionResource;

    public QuestionnaireResponseFhirResource(FollowupActionService followupActionService, FollowupActionQueryService followupActionQueryService, PatientFhirResource patientFhirResource, FollowupActionResource followupActionResource){
        this.followupActionService = followupActionService;
        this.followupActionQueryService = followupActionQueryService;
        this.patientFhirResource = patientFhirResource;
        this.followupActionResource = followupActionResource;
    }

    private final String defaultPath = "localhost:8080/api/fhir/";

    /**
     * Utility private method for transforming a {@link FacetedPage} into a {@link Map} object with results
     * and categories.
     * @param page the page of results
     * @return results as a Map
     */


    /**
     * GET  /followup-action/{id}.
     *
     * @param id the id of the followup-action
     * @return the corresponding followup-action
     */
    @GetMapping("/Questionnaire-response/{id}")
    @Timed
    public String getByFollowupActionId(@PathVariable Long id){
        log.debug("REST request to get questionnaire response in FHIR by followup-action ID", id);
        FollowupAction followupAction = followupActionService.findOne(id);

        org.hl7.fhir.dstu3.model.QuestionnaireResponse questionnaireResponse=
            new org.hl7.fhir.dstu3.model.QuestionnaireResponse();
        org.hl7.fhir.dstu3.model.Reference r = new org.hl7.fhir.dstu3.model.Reference();

        questionnaireResponse.setId(id.toString());
        questionnaireResponse.setStatus(QuestionnaireResponse.QuestionnaireResponseStatus.COMPLETED);
        // add patient as resource url, in the format of fhir standard, json format
        Patient patient = followupAction.getPatient();
//        r.setReference(String.valueOf(patient));
//        String patientInfo = patientFhirResource.getPatient(patient.getId());
        r.setReference(defaultPath + "patients/"+patient.getId());
        questionnaireResponse.setSource(r);


//        FollowupPlan followupPlan = followupAction.getCareEvent().getFollowupPlan();
//        org.hl7.fhir.dstu3.model.Reference r2 = new org.hl7.fhir.dstu3.model.Reference();
//        r2.setReference("localhost:8080/api/fhir/followupPlan/"+followupPlan.getId());
//        questionnaireResponse.setBasedOn(Collections.singletonList(r2));

        // add patient's procedureBooking as resource url, in the format of fhir standard, json format.
        ProcedureBooking procedureBooking = followupAction.getCareEvent().getFollowupPlan().getProcedureBooking();
        org.hl7.fhir.dstu3.model.Reference r4 = new org.hl7.fhir.dstu3.model.Reference();
        r4.setReference(defaultPath + "procedures/"+procedureBooking.getId());
        questionnaireResponse.addParent(r4);

        // add questionnaire patient's need to accomplish, in the format of fhir standard, json format.
        Questionnaire questionnaire = followupAction.getQuestionnaire();
        org.hl7.fhir.dstu3.model.Reference r3 = new org.hl7.fhir.dstu3.model.Reference();
        r3.setReference(defaultPath + "questionnaires/"+questionnaire.getId());
        questionnaireResponse.setQuestionnaire(r3);

        // display each question and its corresponding answer for the questionnaire.
        if(!followupAction.getResponseItems().isEmpty()){
            Set<ResponseItem> responseItems = followupAction.getResponseItems();

            Iterator it1 = responseItems.iterator();
            while(it1.hasNext()){
                ResponseItem responseItem = (ResponseItem) it1.next();
                org.hl7.fhir.dstu3.model.IntegerType i = new org.hl7.fhir.dstu3.model.IntegerType();
//                org.hl7.fhir.dstu3.model.StringType s = new org.hl7.fhir.dstu3.model.StringType();
//                s.setValue(followupAction.getOutcomeComment());
                i.setValue(responseItem.getValue());
                questionnaireResponse.addItem().setLinkId(responseItem.getId().toString()).setText(responseItem.getLocalId()).addAnswer().setValue(i);
            }
            // outcome comment
            org.hl7.fhir.dstu3.model.StringType s = new org.hl7.fhir.dstu3.model.StringType();
            s.setValue(followupAction.getOutcomeComment());
            questionnaireResponse.addItem().addAnswer().setValue(s);

        }

        FhirContext ctx = FhirContext.forDstu3();
        IParser p =ctx.newJsonParser();
        p.setPrettyPrint(true);
        String encode = p.encodeResourceToString(questionnaireResponse);
        return encode;

    }


    /**
     * SEARCH  /_search/followup-actions?query=:query : search for the followupAction corresponding
     * to the query.
     *
     * @param query the query of the followupAction search
     * @param pageable the pagination information
     * @return the result of the search
     */
    @GetMapping("/_search/questionnaire-response")
    @Timed
    public String searchQuestionnaireResponse(@RequestBody QueryModel query, Pageable pageable) {
        log.debug("REST request to search for a page of FollowupActions for query {} in FHIR", query);
        FacetedPage<FollowupAction> page = followupActionService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query.toString(), page, "/api/fhir/_search/questionnaire-response");
        // wrap results page in a response entity with faceted results turned into a map
//        return new ResponseEntity<>(followupActionResource.getMapResults(page), headers, HttpStatus.OK);

        ResponseEntity<Map<String, Object>> responseEntity = new ResponseEntity<>(followupActionResource.getMapResults(page), headers, HttpStatus.OK);

        String questionnaireResponse="[";
        int i=0;
        for(i=0; i<responseEntity.getBody().size()-1; i++){
            Object o = responseEntity.getBody().get(i);
            Long id = Long.parseLong(o.toString());
            questionnaireResponse = questionnaireResponse + getByFollowupActionId(id) + ",";

        }
        Object o1 = responseEntity.getBody().get(i);
        Long id1 = Long.parseLong(o1.toString());
        questionnaireResponse = questionnaireResponse + getByFollowupActionId(id1) + "]";

        return questionnaireResponse;
    }


    // currently not work
    @GetMapping("/questionnaire-responses")
    @Timed
    public String getAllQusetionnaireResponse(Pageable pageable){
        log.debug("REST request to get all questionnaire response in FHIR");
        Page<FollowupAction> page = followupActionService.findAll(pageable);

        String questionnaireRes = "[";
        int i, questionResCount;
        questionResCount = page.getContent().size();
        if (questionResCount == 0){ return "[]";}
        for (i = 0; i < questionResCount - 1; i++){
            questionnaireRes = questionnaireRes + getByFollowupActionId(page.getContent().get(i).getId()) + ",";
        }

        questionnaireRes = questionnaireRes + getByFollowupActionId(page.getContent().get(i).getId()) + "]";
        return questionnaireRes;
    }
}

