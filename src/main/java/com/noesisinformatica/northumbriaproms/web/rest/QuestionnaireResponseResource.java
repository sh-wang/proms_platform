package com.noesisinformatica.northumbriaproms.web.rest;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.parser.JsonParser;
import com.codahale.metrics.annotation.Timed;
import com.noesisinformatica.northumbriaproms.domain.*;
import com.noesisinformatica.northumbriaproms.domain.Patient;
import com.noesisinformatica.northumbriaproms.domain.Questionnaire;
import com.noesisinformatica.northumbriaproms.domain.FollowupAction;
import com.noesisinformatica.northumbriaproms.domain.enumeration.ActionPhase;
import com.noesisinformatica.northumbriaproms.domain.enumeration.ActionStatus;
import com.noesisinformatica.northumbriaproms.service.FollowupActionQueryService;
import com.noesisinformatica.northumbriaproms.service.FollowupActionService;
import com.noesisinformatica.northumbriaproms.service.dto.FollowupActionCriteria;
import com.noesisinformatica.northumbriaproms.web.rest.util.PaginationUtil;
import com.noesisinformatica.northumbriaproms.web.rest.util.QuestionnaireQueryModel;
import io.github.jhipster.web.util.ResponseUtil;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.dstu3.model.Procedure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.FacetedPage;
import org.springframework.data.elasticsearch.core.facet.result.Term;
import org.springframework.data.elasticsearch.core.facet.result.TermResult;
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
@RequestMapping("/api")
public class QuestionnaireResponseResource {
    private final Logger log = LoggerFactory.getLogger(QuestionnaireResponseResource.class);

    private final FollowupActionQueryService followupActionQueryScervice;
    private final FollowupActionService followupActionService;
    //    private final FollowupActionResource followupActionResource;
    public QuestionnaireResponseResource(FollowupActionService followupActionService,
                                         FollowupActionQueryService followupActionQueryService){

        this.followupActionService = followupActionService;
        this.followupActionQueryScervice = followupActionQueryService;
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
    public ResponseEntity<FollowupAction>  getByFollowupActionId(@PathVariable Long id){
        log.debug("REST request to get questionnaire response in FHIR by followup-action ID", id);
        FollowupAction followupAction = followupActionService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(followupAction));
    }

    /**
     * SEARCH  /_search/followup-actions?query=:query : search for the followupAction corresponding
     * to the query.
     * @return the result of the search
     */
    @GetMapping("/Questionnaire-response")
    @Timed
    public ResponseEntity<List<FollowupAction>> searchQuestionnaireResponse(String identifier, String parent,
                                                                            String questionnaire, String status,
                                                                            String patient, String subject,
                                                                            String authored, String author,
                                                                            @PageableDefault(sort = {"id"},
                                                                                direction = Sort.Direction.ASC) Pageable pageable) {

        QuestionnaireQueryModel questionnaireQueryModel = new QuestionnaireQueryModel();
        List<String> emptyValue = new ArrayList();
        List<ActionStatus> queryStatus = new ArrayList<>();
        List<Date> dateEmpty = new ArrayList<>();
        if(authored!=null){
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date date = format.parse(authored);
                questionnaireQueryModel.setAuthored(Collections.singletonList(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }else{
            questionnaireQueryModel.setAuthored(dateEmpty);
        }
        if(author!=null){
            questionnaireQueryModel.setAuthor(Collections.singletonList(author));
        }else{
            questionnaireQueryModel.setAuthor(emptyValue);
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
            switch (status.toUpperCase()) {
                case "IN-PROGRESS":
                    queryStatus.add(ActionStatus.STARTED);
                    queryStatus.add(ActionStatus.PENDING);
                    break;
                case "NULL":
                    queryStatus.add(ActionStatus.UNKNOWN);
                    queryStatus.add(ActionStatus.UNINITIALISED);
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

        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(questionnaireQueryModel.toString(), page, "/api/_search/followup-actions");
        if (page.getTotalElements() == 0){ return new ResponseEntity<>(new ArrayList<>(), headers, HttpStatus.OK); }

        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }





//
//
//    /**
//     * GET  /questionnaire-response : get all the questionnaires
//     * responses by follow up id.
//     *
//     * @param pageable the pagination information
//     * @return all questionnaires response in FHIR
//     */
//    @GetMapping("/Questionnaire-response/all")
//    @Timed
//    public ResponseEntity<String> getAllQuestionnaireResponse(Pageable pageable){
//        log.debug("REST request to get all questionnaire response in FHIR");
//        Page<FollowupAction> page = followupActionService.findAll(pageable);
//        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page,
//            "/api/fhir/Questionnaire-response/all");
//        if (page.getTotalElements() == 0){ return new ResponseEntity<>("[]", headers, HttpStatus.OK); }
//
//        JsonArray quesResArray = JsonConversion(page);
//
//        return new ResponseEntity<>(quesResArray.toString(), headers, HttpStatus.OK);
//    }
//
//
//    /**
//     * Convert a list of FHIR follow up actions into a Json array
//     *
//     * @param page the page store all questionnaire response
//     * @return the Json array contains all follow up actions
//     */
//    private JsonArray JsonConversion(Page page){
//        List<FollowupAction> quesResList = new ArrayList<>();
//        JsonArray quesResArray = new JsonArray();
//        String questionnaireResponseString;
//        JsonObject quesResJson;
//        int pageNumber = page.getTotalPages();
//
//        //get all questionnaire response
//        while(pageNumber > 0){
//            quesResList.addAll(page.getContent());
//            page = followupActionService.findAll(page.nextPageable());
//            pageNumber--;
//        }
//
//        for(FollowupAction followupAction: quesResList) {
//            questionnaireResponseString = getByFollowupActionId(followupAction.getId()).getBody();
//            com.google.gson.JsonParser toJson = new com.google.gson.JsonParser();
//            quesResJson = toJson.parse(questionnaireResponseString).getAsJsonObject();
//            quesResArray.add(quesResJson);
//        }
//        return quesResArray;
//    }

}

