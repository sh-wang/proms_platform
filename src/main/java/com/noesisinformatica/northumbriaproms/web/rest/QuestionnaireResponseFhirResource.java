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
import com.noesisinformatica.northumbriaproms.web.rest.util.PaginationUtil;
import org.hl7.fhir.dstu3.model.*;
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
<<<<<<< HEAD

    public QuestionnaireResponseFhirResource( FollowupActionService followupActionService, FollowupActionQueryService followupActionQueryService, PatientFhirResource patientFhirResource, FollowupActionResource followupActionResource){
=======
    private final ProcedureFhirResource procedureFhirResource;
    private final QuestionnaireFhirResource questionnaireFhirResource;
    public QuestionnaireResponseFhirResource(FollowupActionService followupActionService, FollowupActionQueryService followupActionQueryService, PatientFhirResource patientFhirResource, FollowupActionResource followupActionResource, ProcedureFhirResource procedureFhirResource,QuestionnaireFhirResource questionnaireFhirResource){
>>>>>>> d2fb4d8bb1175bf9c231c49b0f45ea6595c1bfc5
        this.followupActionService = followupActionService;
        this.followupActionQueryService = followupActionQueryService;
        this.patientFhirResource = patientFhirResource;
        this.followupActionResource = followupActionResource;
        this.procedureFhirResource = procedureFhirResource;
        this.questionnaireFhirResource = questionnaireFhirResource;
    }

    private final String defaultPath = "localhost:8080/api/fhir/";


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


        questionnaireResponse.setId(id.toString());
        questionnaireResponse.setStatus(QuestionnaireResponse.QuestionnaireResponseStatus.COMPLETED);
        // add patient as resource url, in the format of fhir standard, json format
        Patient patient = followupAction.getPatient();
//        r.setReference(String.valueOf(patient));
        String patientInfo = patientFhirResource.getPatient(patient.getId());
//        r.setReference(defaultPath + "patients/"+patient.getId());
////        questionnaireResponse.setSource(r);
        patientInfo.replaceAll("\\\\([\"/])", "$1");
        org.hl7.fhir.dstu3.model.Reference r = new org.hl7.fhir.dstu3.model.Reference(patientInfo);
        questionnaireResponse.setSource(r);

//        FollowupPlan followupPlan = followupAction.getCareEvent().getFollowupPlan();
//        org.hl7.fhir.dstu3.model.Reference r2 = new org.hl7.fhir.dstu3.model.Reference();
//        r2.setReference("localhost:8080/api/fhir/followupPlan/"+followupPlan.getId());
//        questionnaireResponse.setBasedOn(Collections.singletonList(r2));

        // add patient's procedureBooking as resource url, in the format of fhir standard, json format.
        ProcedureBooking procedureBooking = followupAction.getCareEvent().getFollowupPlan().getProcedureBooking();
        org.hl7.fhir.dstu3.model.Reference r4 = new org.hl7.fhir.dstu3.model.Reference();
//        r4.setReference(defaultPath + "procedures/"+procedureBooking.getId());
        String procedure = procedureFhirResource.getProcedure(procedureBooking.getId());
        r4.setReference(procedure);
        questionnaireResponse.addParent(r4);

        // add questionnaire patient's need to accomplish, in the format of fhir standard, json format.
        Questionnaire questionnaire = followupAction.getQuestionnaire();
        org.hl7.fhir.dstu3.model.Reference r3 = new org.hl7.fhir.dstu3.model.Reference();
        String questionnairejson = questionnaireFhirResource.getQuestionnaire(questionnaire.getId());
//        r3.setReference(defaultPath + "questionnaires/"+questionnaire.getId());
        r3.setReference(questionnairejson);
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
<<<<<<< HEAD
    @GetMapping("/_search/followup-actions")
=======
    @GetMapping("/Qquestionnaire-response")
>>>>>>> d2fb4d8bb1175bf9c231c49b0f45ea6595c1bfc5
    @Timed
    public String searchQuestionnaireResponse(@RequestParam String query, Pageable pageable) {
        log.debug("REST request to search for a page of FollowupActions in FHIR format for query {}", query);
        Page<FollowupAction> page = followupActionService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/fhir/_search/followup-actions");


        ResponseEntity<List<FollowupAction>> responseEntity = new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);


        // identical to method above, but query only supports NHS number and patient's name
        String questionnaireResponses = "[";
        int i;
        if(responseEntity.getBody().size() == 0){ return "[]"; }
        for (i = 0; i < responseEntity.getBody().size() - 1; i++) {
            Object o = responseEntity.getBody().get(i);
            Long id = ((FollowupAction) o).getId();
            questionnaireResponses += getByFollowupActionId(id) + ",";
        }

        Object o1 = responseEntity.getBody().get(i);
        Long id1 =((FollowupAction) o1).getId();
        questionnaireResponses += getByFollowupActionId(id1) ;

        return questionnaireResponses;
    }


    // currently not work
    @GetMapping("/Questionnaire-response/all")
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


//    public String myJsonFilter(String foo) {
//        foo.replaceAll("\"", "\\\"");
//        foo.replaceAll("\"", "\\\\\"");
//        foo.replace("\"", "\\\"");
//        return foo;
//    }
}

