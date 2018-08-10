package com.noesisinformatica.northumbriaproms.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.noesisinformatica.northumbriaproms.domain.*;
import com.noesisinformatica.northumbriaproms.domain.FollowupAction;
import com.noesisinformatica.northumbriaproms.domain.enumeration.ActionPhase;
import com.noesisinformatica.northumbriaproms.domain.enumeration.ActionStatus;
import com.noesisinformatica.northumbriaproms.service.FollowupActionService;
import com.noesisinformatica.northumbriaproms.web.rest.util.PaginationUtil;
import com.noesisinformatica.northumbriaproms.web.rest.util.QuestionnaireQueryModel;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.*;

/**
 * REST controller for Resource QuestionnaireResponse.
 */
@RestController
@RequestMapping("/api")
public class QuestionnaireResponseResource {
    private final Logger log = LoggerFactory.getLogger(QuestionnaireResponseResource.class);

    private final FollowupActionService followupActionService;

    public QuestionnaireResponseResource(FollowupActionService followupActionService) {

        this.followupActionService = followupActionService;
    }


    /**
     * GET  /followup-action/{id}.
     *
     * @param id the id of the followup-action
     * @return the corresponding followup-action
     */
    @GetMapping("/Questionnaire-response/{id}")
    @Timed
    public ResponseEntity<FollowupAction> getByFollowupActionId(@PathVariable Long id) {
        log.debug("REST request to get questionnaire response in FHIR by followup-action ID", id);
        FollowupAction followupAction = followupActionService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(followupAction));
    }

    /**
     * SEARCH  /_search/followup-actions?query=:query : search for the followupAction corresponding
     * to the query.
     *
     * @return the result of the search
     */
    @GetMapping("/search/Questionnaire-response")
    @Timed
    public ResponseEntity<List<FollowupAction>> searchQuestionnaireResponse(String identifier, String parent,
                                                                            String questionnaire, String status,
                                                                            String patient, String familyName,
                                                                            String authored, String author,
                                                                            @PageableDefault(sort = {"id"},
                                                                                direction = Sort.Direction.ASC) Pageable pageable) {

        QuestionnaireQueryModel questionnaireQueryModel = new QuestionnaireQueryModel();
        List<String> emptyValue = new ArrayList();
        List<ActionStatus> queryStatus = new ArrayList<>();
        List<Date> dateEmpty = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        if (authored != null) {
            try {
                Date date = format.parse(authored);
                questionnaireQueryModel.setAuthored(Collections.singletonList(date));
            } catch (ParseException e) {
                e.printStackTrace();
                try { Date fakeDate = format.parse("1000-01-01");
                    questionnaireQueryModel.setAuthored(Collections.singletonList(fakeDate));
                }catch (ParseException w){}
            }
        } else {
            questionnaireQueryModel.setAuthored(dateEmpty);
        }
        if (author != null) {
            questionnaireQueryModel.setAuthor(Collections.singletonList(author));
        } else {
            questionnaireQueryModel.setAuthor(emptyValue);
        }
        if (identifier != null) {
            questionnaireQueryModel.setIdentifier(Collections.singletonList(identifier));
        } else {
            questionnaireQueryModel.setIdentifier(emptyValue);
        }
        if (parent != null) {
            questionnaireQueryModel.setParent(Collections.singletonList(parent));
        } else {
            questionnaireQueryModel.setParent(emptyValue);
        }
        if (questionnaire != null) {
            questionnaireQueryModel.setQuestionnaire(Collections.singletonList(questionnaire));
        } else {
            questionnaireQueryModel.setQuestionnaire(emptyValue);
        }
        if (status != null) {
            switch (status.toUpperCase()) {
                case "IN-PROGRESS":
                    queryStatus.add(ActionStatus.STARTED);
                    queryStatus.add(ActionStatus.PENDING);
                    break;
                case "STOPPED":
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
        } else {
            questionnaireQueryModel.setStatus(queryStatus);
        }
        if (patient != null) {
            questionnaireQueryModel.setPatient(Collections.singletonList(patient));
        } else {
            questionnaireQueryModel.setPatient(emptyValue);
        }
        if (familyName != null) {
            questionnaireQueryModel.setFamilyName(Collections.singletonList(familyName));
        } else {
            questionnaireQueryModel.setFamilyName(emptyValue);
        }

        log.debug("REST request to search for a page of QuestionnaireResponse for query {}", questionnaireQueryModel);
        Page<FollowupAction> page = followupActionService.searchQuestionnaire(questionnaireQueryModel, pageable);

        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(questionnaireQueryModel.toString(), page, "/api/_search/followup-actions");
        if (page.getTotalElements() == 0) {
            return new ResponseEntity<>(new ArrayList<>(), headers, HttpStatus.OK);
        }
        System.out.println("result:"+page.getContent());
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }


}
