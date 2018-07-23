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
import com.noesisinformatica.northumbriaproms.domain.enumeration.ActionPhase;
import com.noesisinformatica.northumbriaproms.domain.enumeration.ActionStatus;
import com.noesisinformatica.northumbriaproms.service.FollowupActionQueryService;
import com.noesisinformatica.northumbriaproms.service.FollowupActionService;
import com.noesisinformatica.northumbriaproms.service.dto.FollowupActionCriteria;
import com.noesisinformatica.northumbriaproms.web.rest.util.PaginationUtil;
import com.noesisinformatica.northumbriaproms.web.rest.util.QuestionnaireQueryModel;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.dstu3.model.Procedure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.FacetedPage;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.spring.web.json.Json;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.time.ZoneId;
import java.time.ZonedDateTime;
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

    private FhirContext ctx = FhirContext.forDstu3();
    private IParser p =ctx.newJsonParser();

    /**
     * GET  /followup-action/{id}.
     *
     * @param id the id of the followup-action
     * @return the corresponding followup-action
     */
    @GetMapping("/Questionnaire-response/{id}")
    @Timed
    public ResponseEntity<String> getByFollowupActionId(@PathVariable Long id){
        log.debug("REST request to get questionnaire response in FHIR by followup-action ID", id);

        org.hl7.fhir.dstu3.model.QuestionnaireResponse questionnaireResponse = getQuestionnaireResponseResource(id);
        if (questionnaireResponse == null){return new ResponseEntity<>("[]", HttpStatus.OK);}

        String encode = p.encodeResourceToString(questionnaireResponse);
        return new ResponseEntity<>(encode, HttpStatus.OK);
    }


    private QuestionnaireResponse getQuestionnaireResponseResource(Long id){
        FollowupAction followupAction = followupActionService.findOne(id);
        if (followupAction == null){return null;}
        org.hl7.fhir.dstu3.model.QuestionnaireResponse questionnaireResponse=
            new org.hl7.fhir.dstu3.model.QuestionnaireResponse();
        questionnaireResponse.setId(id.toString());

//        questionnaireResponse.setStatus(QuestionnaireResponse.QuestionnaireResponseStatus.COMPLETED);
        if (followupAction.getStatus().equals(ActionStatus.STARTED)){
            questionnaireResponse.setStatus(QuestionnaireResponse.QuestionnaireResponseStatus.INPROGRESS);
        }
        if (followupAction.getStatus().equals(ActionStatus.UNINITIALISED)){
            questionnaireResponse.setStatus(QuestionnaireResponse.QuestionnaireResponseStatus.NULL);
        }
        if (followupAction.getStatus().equals(ActionStatus.COMPLETED)){
            questionnaireResponse.setStatus(QuestionnaireResponse.QuestionnaireResponseStatus.COMPLETED);
        }
        if (followupAction.getStatus().equals(ActionStatus.UNKNOWN)){
            questionnaireResponse.setStatus(QuestionnaireResponse.QuestionnaireResponseStatus.NULL);
        }
        if (followupAction.getStatus().equals(ActionStatus.PENDING)){
            questionnaireResponse.setStatus(QuestionnaireResponse.QuestionnaireResponseStatus.INPROGRESS);
        }

        org.hl7.fhir.dstu3.model.Patient patientFHIR = patientFhirResource.getPatientResource(
            followupAction.getPatient().getId());
        org.hl7.fhir.dstu3.model.Reference refePa = new org.hl7.fhir.dstu3.model.Reference(patientFHIR);
        questionnaireResponse.setSubject(refePa);

        Procedure procedureFHIR = procedureFhirResource.getProcedureResource(followupAction.getCareEvent()
            .getFollowupPlan().getProcedureBooking().getId());
        org.hl7.fhir.dstu3.model.Reference refePr = new org.hl7.fhir.dstu3.model.Reference(procedureFHIR);
        questionnaireResponse.addParent(refePr);

        //add completed date
        try{
            ZonedDateTime completedDate = followupAction.getCompletedDate().atStartOfDay(ZoneId.systemDefault());
            questionnaireResponse.setAuthored(Date.from(completedDate.toInstant()));
        }catch (Exception e){ }

        // add patient's questionnaire need to accomplish, in the format of fhir standard, json format.
        org.hl7.fhir.dstu3.model.Questionnaire questionnaireFHIR = questionnaireFhirResource
            .getQuestionnaireResource(followupAction.getQuestionnaire().getId());
        org.hl7.fhir.dstu3.model.Reference refeQu = new org.hl7.fhir.dstu3.model.Reference(questionnaireFHIR);
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
                questionnaireResponse.addItem().setLinkId(responseItem.getId().
                    toString()).setText(responseItem.getLocalId()).addAnswer().setValue(i);
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
     * @return the result of the search
     */
    @GetMapping("/Questionnaire-response")
    @Timed
    public ResponseEntity<String> searchQuestionnaireResponse(String identifier, String parent,
                                                              String questionnaire, String status,
                                                              String patient, String subject,
                                                              String authored,
                                                              @PageableDefault(sort = {"id"},
                                                                  direction = Sort.Direction.ASC) Pageable pageable) {


        if(authored==null && identifier==null && parent==null && questionnaire==null && status==null && patient==null && subject==null){
            return new ResponseEntity<>("[]", HttpStatus.OK);
        }
        QuestionnaireQueryModel questionnaireQueryModel = new QuestionnaireQueryModel();
        List<String> emptyValue = new ArrayList();
        List<ActionStatus> queryStatus = new ArrayList<>();
        List<Date> dateempty = new ArrayList<>();
        if(authored!=null){
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            try {
                Date date = format.parse(authored);
                questionnaireQueryModel.setAuthored(Collections.singletonList(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }else{
            questionnaireQueryModel.setAuthored(dateempty);
        }

        if(identifier!=null){
            questionnaireQueryModel.setIdentifier(Collections.singletonList(identifier));
        }else{
            questionnaireQueryModel.setIdentifier(emptyValue);
        }
        if(parent!=null){
            questionnaireQueryModel.setParent(Collections.singletonList(parent));
        }else {
            questionnaireQueryModel.setParent(emptyValue);
        }
        if(questionnaire!=null){
            questionnaireQueryModel.setQuestionnaire(Collections.singletonList(questionnaire));
        }else{
            questionnaireQueryModel.setQuestionnaire(emptyValue);
        }
        if(status!=null){
            switch (status) {
                case "inprogress":
                    queryStatus.add(ActionStatus.STARTED);
                    queryStatus.add(ActionStatus.PENDING);
                    break;
                case "INPROGRESS":
                    queryStatus.add(ActionStatus.STARTED);
                    queryStatus.add(ActionStatus.PENDING);
                    break;
                case "?":
                    queryStatus.add(ActionStatus.UNKNOWN);
                    queryStatus.add(ActionStatus.UNINITIALISED);
                    break;
                case "completed":
                    queryStatus.add(ActionStatus.COMPLETED);
                    break;
                case "COMPLETED":
                    queryStatus.add(ActionStatus.COMPLETED);
                    break;
                default:
                    queryStatus.add(ActionStatus.FORSEARCH);
                    break;
            }
            questionnaireQueryModel.setStatus(queryStatus);
        }else{
            questionnaireQueryModel.setStatus(queryStatus);
        }
        if(patient!=null){
            questionnaireQueryModel.setPatient(Collections.singletonList(patient));
        }else{
            questionnaireQueryModel.setPatient(emptyValue);
        }
        if(subject!=null){
            questionnaireQueryModel.setSubject(Collections.singletonList(subject));
        }else{
            questionnaireQueryModel.setSubject(emptyValue);
        }

        log.debug("REST request to search for a page of FollowupActions for query {}", questionnaireQueryModel);
        Page<FollowupAction> page = followupActionService.searchQuestionnaire(questionnaireQueryModel, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(questionnaireQueryModel.toString(),
            page, "/api/fhir/Questionnaire-response");
        if (page.getTotalElements() == 0){ return new ResponseEntity<>("[]", headers, HttpStatus.OK); }

        // wrap results page in a response entity with faceted results turned into a map
        JsonArray QuesResArray = JsonConversion(page);

        return new ResponseEntity<>(QuesResArray.toString(), headers, HttpStatus.OK);

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
    public ResponseEntity<String> getAllQuestionnaireResponse(Pageable pageable){
        log.debug("REST request to get all questionnaire response in FHIR");
        Page<FollowupAction> page = followupActionService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page,
            "/api/fhir/Questionnaire-response/all");
        if (page.getTotalElements() == 0){ return new ResponseEntity<>("[]", headers, HttpStatus.OK); }

        JsonArray quesResArray = JsonConversion(page);

        return new ResponseEntity<>(quesResArray.toString(), headers, HttpStatus.OK);
    }


    /**
     * Convert a list of FHIR follow up actions into a Json array
     *
     * @param page the page store all questionnaire response
     * @return the Json array contains all follow up actions
     */
    private JsonArray JsonConversion(Page page){
        List<FollowupAction> quesResList = new ArrayList<>();
        JsonArray quesResArray = new JsonArray();
        String questionnaireResponseString;
        JsonObject quesResJson;
        int pageNumber = page.getTotalPages();

        //get all questionnaire response
        while(pageNumber > 0){
            quesResList.addAll(page.getContent());
            page = followupActionService.findAll(page.nextPageable());
            pageNumber--;
        }

        for(FollowupAction followupAction: quesResList) {
            questionnaireResponseString = getByFollowupActionId(followupAction.getId()).getBody();
            com.google.gson.JsonParser toJson = new com.google.gson.JsonParser();
            quesResJson = toJson.parse(questionnaireResponseString).getAsJsonObject();
            quesResArray.add(quesResJson);
        }
        return quesResArray;
    }
}

