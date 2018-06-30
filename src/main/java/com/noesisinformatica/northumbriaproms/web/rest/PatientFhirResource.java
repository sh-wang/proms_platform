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
import com.noesisinformatica.northumbriaproms.domain.Patient;
import com.noesisinformatica.northumbriaproms.domain.enumeration.GenderType;
import com.noesisinformatica.northumbriaproms.service.PatientService;
import org.hl7.fhir.dstu3.model.ContactPoint;
import org.hl7.fhir.dstu3.model.Enumerations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * REST controller for managing Patient.
 */
@RestController
@RequestMapping("/fhirapi")
public class PatientFhirResource {
    private final Logger log = LoggerFactory.getLogger(PatientFhirResource.class);

    private static final String ENTITY_NAME = "patient";

    private final PatientService patientService;
    public PatientFhirResource(PatientService patientService) {
        this.patientService = patientService;
    }


    /**
     * GET  /patients/:id : get the "id" patient.
     *
     * @param id the id of the patient to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the patient, or with status 404 (Not Found)
     */
    @GetMapping("/patients/{id}")
    @Timed
    public String getPatient(@PathVariable Long id) {
        log.debug("REST request to get Patient : {}", id);
        Patient patient = patientService.findOne(id);
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
        FhirContext ctx = FhirContext.forDstu3();
        IParser p =ctx.newJsonParser();
        p.setPrettyPrint(true);
        String encode = p.encodeResourceToString(patientFhir);
        return encode;
    }
}
