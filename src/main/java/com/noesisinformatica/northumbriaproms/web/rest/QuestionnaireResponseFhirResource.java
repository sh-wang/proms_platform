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
import com.noesisinformatica.northumbriaproms.domain.FollowupAction;
import com.noesisinformatica.northumbriaproms.domain.Patient;
import com.noesisinformatica.northumbriaproms.domain.Questionnaire;
import com.noesisinformatica.northumbriaproms.domain.enumeration.GenderType;
import com.noesisinformatica.northumbriaproms.service.FollowupActionQueryService;
import com.noesisinformatica.northumbriaproms.service.PatientService;
import com.noesisinformatica.northumbriaproms.service.QuestionnaireService;
import com.noesisinformatica.northumbriaproms.service.dto.FollowupActionCriteria;
import io.github.jhipster.service.filter.LongFilter;
import org.hl7.fhir.dstu3.model.ContactPoint;
import org.hl7.fhir.dstu3.model.Enumerations;
import org.hl7.fhir.dstu3.model.QuestionnaireResponse;
import org.hl7.fhir.dstu3.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * REST controller for Resource QuestionnaireResponse.
 */
@RestController
@RequestMapping("/fhirapi")
public class QuestionnaireResponseFhirResource {
    private final Logger log = LoggerFactory.getLogger(QuestionnaireResponseFhirResource.class);

    private final PatientService patientService;
    private final QuestionnaireService questionnaireService;
    private final FollowupActionQueryService followupActionQueryService;

    public QuestionnaireResponseFhirResource(PatientService patientService, QuestionnaireService questionnaireService, FollowupActionQueryService followupActionQueryService){
        this.patientService = patientService;
        this.questionnaireService = questionnaireService;
        this.followupActionQueryService = followupActionQueryService;
    }

    /**
        * GET  /patient/:id/Questionnaire/:QuestionnaireId.
        *
        * @param patientId the id of the patient
     * @return the ResponseEntity with status 200 (OK) and with body the QuestionnaireResponse, or with status 404 (Not Found)
        */
    @GetMapping("patient/{patientId}/questionnaireResponse")
    @Timed
    public String getByPatientId(@PathVariable Long patientId){
        org.hl7.fhir.dstu3.model.QuestionnaireResponse questionnaireResponse= new org.hl7.fhir.dstu3.model.QuestionnaireResponse();


        FollowupActionCriteria criteria = new FollowupActionCriteria();
        LongFilter filter = new LongFilter();
        filter.setEquals(patientId);
        criteria.setPatientId(filter);
        List<FollowupAction> followupActions = followupActionQueryService.findByCriteria(criteria);



        if(followupActions.size()>0){
            Patient patient = followupActions.get(0).getPatient();


            org.hl7.fhir.dstu3.model.Patient patientFhir = new org.hl7.fhir.dstu3.model.Patient();
            patientFhir.addName().setFamily(patient.getFamilyName()).addGiven(patient.getGivenName());

            ZoneId zoneId = ZoneId.systemDefault();
            ZonedDateTime btd = patient.getBirthDate().atStartOfDay(zoneId);
            patientFhir.setBirthDate(Date.from(btd.toInstant()));
            patientFhir.addTelecom().setSystem(ContactPoint.ContactPointSystem.EMAIL).setValue(patient.getEmail());
            patientFhir.addIdentifier().setSystem("NHS").setValue(patient.getNhsNumber().toString());
            if (patient.getGender().equals(GenderType.MALE)){
                patientFhir.setGender(Enumerations.AdministrativeGender.MALE);
            }else if(patient.getGender().equals(GenderType.FEMALE)){
                patientFhir.setGender(Enumerations.AdministrativeGender.FEMALE);
            }else if(patient.getGender().equals(GenderType.OTHER)){
                patientFhir.setGender(Enumerations.AdministrativeGender.OTHER);
            }else if(patient.getGender().equals(GenderType.UNKNOWN)){
                patientFhir.setGender(Enumerations.AdministrativeGender.UNKNOWN);
            }

            questionnaireResponse.addContained(patientFhir);
            for(int i = 0; i<followupActions.size(); i++){
                Questionnaire questionnaire = followupActions.get(i).getQuestionnaire();
                org.hl7.fhir.dstu3.model.Questionnaire questionnaireFhir = new org.hl7.fhir.dstu3.model.Questionnaire();
                questionnaireFhir.setId(questionnaire.getId().toString());
                questionnaireResponse.addItem().setLinkId(questionnaire.getId().toString()).setDefinition(questionnaire.getCopyright());

            }
        }

        questionnaireResponse.setId("1");
        questionnaireResponse.setStatus(QuestionnaireResponse.QuestionnaireResponseStatus.COMPLETED);

        FhirContext ctx = FhirContext.forDstu3();
        IParser p =ctx.newJsonParser();
        p.setPrettyPrint(true);
        String encode = p.encodeResourceToString(questionnaireResponse);
        return encode;
    }
}
