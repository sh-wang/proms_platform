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
import ca.uhn.fhir.parser.JsonParser;
import com.codahale.metrics.annotation.Timed;
import com.noesisinformatica.northumbriaproms.domain.*;
import com.noesisinformatica.northumbriaproms.domain.Patient;
import com.noesisinformatica.northumbriaproms.domain.Questionnaire;
import com.noesisinformatica.northumbriaproms.service.FollowupActionQueryService;
import com.noesisinformatica.northumbriaproms.service.FollowupActionService;
import com.noesisinformatica.northumbriaproms.service.dto.FollowupActionCriteria;
import com.noesisinformatica.northumbriaproms.web.rest.util.PaginationUtil;
import com.noesisinformatica.northumbriaproms.web.rest.util.QueryModel;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.dstu3.model.Procedure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.FacetedPage;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.spring.web.json.Json;

import java.util.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * REST controller for Resource QuestionnaireResponse.
 */
@RestController
@RequestMapping("/api/fhir")
public class QuestionnaireResponseFhirResource {
    private final Logger log = LoggerFactory.getLogger(QuestionnaireResponseFhirResource.class);

    private final FollowupActionQueryService followupActionQueryScervice;
    private final FollowupActionService followupActionService;
    private final PatientFhirResource patientFhirResource;
    //    private final FollowupActionResource followupActionResource;
    private final ProcedureFhirResource procedureFhirResource;
    private final QuestionnaireFhirResource questionnaireFhirResource;
    public QuestionnaireResponseFhirResource(FollowupActionService followupActionService,
                                             FollowupActionQueryService followupActionQueryService,
                                             PatientFhirResource patientFhirResource,
//                                             FollowupActionResource followupActionResource,
                                             ProcedureFhirResource procedureFhirResource,
                                             QuestionnaireFhirResource questionnaireFhirResource){

        this.followupActionService = followupActionService;
        this.followupActionQueryScervice = followupActionQueryService;
        this.patientFhirResource = patientFhirResource;
//        this.followupActionResource = followupActionResource;
        this.procedureFhirResource = procedureFhirResource;
        this.questionnaireFhirResource = questionnaireFhirResource;
    }

//    private final String defaultPath = "localhost:8080/api/fhir/";


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
        if (followupAction == null){return "[]";}
        org.hl7.fhir.dstu3.model.QuestionnaireResponse questionnaireResponse = getQuestionnaireResponseResource(id);

        FhirContext ctx = FhirContext.forDstu3();
        IParser p =ctx.newJsonParser();
        p.setPrettyPrint(false);
        String encode = p.encodeResourceToString(questionnaireResponse);
        return encode;
    }


    public QuestionnaireResponse getQuestionnaireResponseResource(Long id){
        FollowupAction followupAction = followupActionService.findOne(id);
        if (followupAction == null){return null;}
        org.hl7.fhir.dstu3.model.QuestionnaireResponse questionnaireResponse=
            new org.hl7.fhir.dstu3.model.QuestionnaireResponse();
        questionnaireResponse.setId(id.toString());
        questionnaireResponse.setStatus(QuestionnaireResponse.QuestionnaireResponseStatus.COMPLETED);

        org.hl7.fhir.dstu3.model.Patient patientFHIR = patientFhirResource.getPatientResource(
            followupAction.getPatient().getId());
        org.hl7.fhir.dstu3.model.Reference refePa = new org.hl7.fhir.dstu3.model.Reference(patientFHIR);
//        questionnaireResponse.setSource(refePa);
        questionnaireResponse.setSubject(refePa);

        Procedure procedureFHIR = procedureFhirResource.getProcedureResource(followupAction.getCareEvent()
            .getFollowupPlan().getProcedureBooking().getId());
        org.hl7.fhir.dstu3.model.Reference refePr = new org.hl7.fhir.dstu3.model.Reference(procedureFHIR);
//        r4.setReference(procedureFHIR);
        questionnaireResponse.addParent(refePr);

        // add patient's questionnaire need to accomplish, in the format of fhir standard, json format.
        org.hl7.fhir.dstu3.model.Questionnaire questionnaireFHIR = questionnaireFhirResource
            .getQuestionnaireResource(followupAction.getQuestionnaire().getId());
        org.hl7.fhir.dstu3.model.Reference refeQu = new org.hl7.fhir.dstu3.model.Reference(questionnaireFHIR);
//        r3.setReference(questionnairejson);
        questionnaireResponse.setQuestionnaire(refeQu);

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

        String author = followupAction.getCreatedBy();
        Reference authorRef = new Reference(author);
        questionnaireResponse.setAuthor(authorRef);

        return questionnaireResponse;
    }

    /**
     * SEARCH  /_search/followup-actions?query=:query : search for the followupAction corresponding
     * to the query.
     *
     * @param pageable the pagination information
     * @return the result of the search
     */
    @GetMapping("/Questionnaire-response")
    @Timed
    public ResponseEntity<String> searchQuestionnaireResponse(String procedures, String consultants,
                                                              String locations, String patientIds,
                                                              String phases, String types,
                                                              String genders, String sides,
                                                              String statuses,
                                                              String careEvents,
                                                              Integer minAge, Integer maxAge,
                                                              String token, Pageable pageable) {
        QueryModel query = new QueryModel();
        query.setProcedures(Collections.singletonList(procedures));
        query.setConsultants(Collections.singletonList(consultants));
        query.setLocations(Collections.singletonList(locations));
        query.setCareEvents(Collections.singletonList(careEvents));
        query.setPatientIds(Collections.singletonList(patientIds));
        query.setGenders(Collections.singletonList(genders));
        query.setPhases(Collections.singletonList(phases));
        query.setMaxAge(maxAge);
        query.setMinAge(minAge);
        query.setTypes(Collections.singletonList(types));
        query.setSides(Collections.singletonList(sides));
        query.setStatuses(Collections.singletonList(statuses));
        query.setToken(token);

        System.out.println(query);

        log.debug("REST request to search for a page of FollowupActions for query {}", query);
        FacetedPage<FollowupAction> page = followupActionService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query.toString(),
            page, "/api/fhir/Questionnaire-response");
        // wrap results page in a response entity with faceted results turned into a map

        List<FollowupAction> actionList = page.getContent();
        JsonArray QuesResarray = new JsonArray();

        for(FollowupAction followupAction: actionList) {
            String questionnaireResponseString = getByFollowupActionId(followupAction.getId());
            com.google.gson.JsonParser toJson = new com.google.gson.JsonParser();
            JsonObject quesResJson = toJson.parse(questionnaireResponseString).getAsJsonObject();
            QuesResarray.add(quesResJson);
        }

        return new ResponseEntity<>(QuesResarray.toString(), headers, HttpStatus.OK);
    }



        /**
         * GET  /questionnaire-response : get all the questionnaires
         * responses by follow up id.
         *
         * @param pageable the pagination information
         * @return all questionnaires response in FHIR
         */
    @GetMapping("/Questionnaire-response/all")
    @Timed
    public ResponseEntity<String> getAllQuestionnaireResponse(FollowupActionCriteria criteria,Pageable pageable){
        log.debug("REST request to get all questionnaire response in FHIR");
        Page<FollowupAction> page = followupActionQueryScervice.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page,
            "/api/fhir/Questionnaire-response/all");

        List<FollowupAction> actionList = page.getContent();
        JsonArray QuesResarray = new JsonArray();

        for(FollowupAction followupAction: actionList) {
            String questionnaireResponseString = getByFollowupActionId(followupAction.getId());
            com.google.gson.JsonParser toJson = new com.google.gson.JsonParser();
            JsonObject quesResJson = toJson.parse(questionnaireResponseString).getAsJsonObject();
            QuesResarray.add(quesResJson);
        }

        return new ResponseEntity<>(QuesResarray.toString(), headers, HttpStatus.OK);
    }
}

