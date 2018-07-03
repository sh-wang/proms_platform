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
import io.github.jhipster.service.filter.LongFilter;
import org.hl7.fhir.dstu3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * REST controller for Resource QuestionnaireResponse.
 */
@RestController
@RequestMapping("/api")
public class QuestionnaireResponseFhirResource {
    private final Logger log = LoggerFactory.getLogger(QuestionnaireResponseFhirResource.class);

    private final FollowupActionQueryService followupActionQueryService;
    private final FollowupActionService followupActionService;

    public QuestionnaireResponseFhirResource(FollowupActionService followupActionService, FollowupActionQueryService followupActionQueryService){
        this.followupActionService = followupActionService;
        this.followupActionQueryService = followupActionQueryService;
    }


    /**
     * GET  /followup-action/{id}.
     *
     * @param id the id of the followup-action
     * @return the ResponseEntity with status 200 (OK) and with body the QuestionnaireResponse, or with status 404 (Not Found)
     */
    @GetMapping("/fhir/followup-action/{id}")
    @Timed
    public String getByPatientId(@PathVariable Long id){
        FollowupAction followupAction = followupActionService.findOne(id);
        org.hl7.fhir.dstu3.model.QuestionnaireResponse questionnaireResponse= new org.hl7.fhir.dstu3.model.QuestionnaireResponse();
        org.hl7.fhir.dstu3.model.Reference r = new org.hl7.fhir.dstu3.model.Reference();
        questionnaireResponse.setId("2");
        questionnaireResponse.setStatus(QuestionnaireResponse.QuestionnaireResponseStatus.COMPLETED);
        Patient patient = followupAction.getPatient();
        r.setReference(String.valueOf(patient));
        questionnaireResponse.setSource(r);

        FollowupPlan followupPlan = followupAction.getCareEvent().getFollowupPlan();
        org.hl7.fhir.dstu3.model.Reference r2 = new org.hl7.fhir.dstu3.model.Reference();
        r2.setReference(String.valueOf(followupPlan));
        questionnaireResponse.setBasedOn(Collections.singletonList(r2));

        Questionnaire questionnaire = followupAction.getQuestionnaire();
        org.hl7.fhir.dstu3.model.Reference r3 = new org.hl7.fhir.dstu3.model.Reference();
        r3.setReference(String.valueOf(questionnaire));
        questionnaireResponse.setQuestionnaire(r3);

        if(!followupAction.getResponseItems().isEmpty()){
            Set<ResponseItem> responseItems = followupAction.getResponseItems();

            Iterator it1 = responseItems.iterator();
            while(it1.hasNext()){
                ResponseItem responseItem = (ResponseItem) it1.next();
                org.hl7.fhir.dstu3.model.IntegerType i = new org.hl7.fhir.dstu3.model.IntegerType();
                i.setValue(responseItem.getValue());

                questionnaireResponse.addItem().setLinkId(responseItem.getId().toString()).setText(responseItem.getLocalId()).addAnswer().setValue(i);

            }

        }

        FhirContext ctx = FhirContext.forDstu3();
        IParser p =ctx.newJsonParser();
        p.setPrettyPrint(true);
        String encode = p.encodeResourceToString(questionnaireResponse);
        return encode;

    }
}
