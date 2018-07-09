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
import com.noesisinformatica.northumbriaproms.service.PatientQueryService;
import com.noesisinformatica.northumbriaproms.service.PatientService;
import com.noesisinformatica.northumbriaproms.web.rest.util.PaginationUtil;
import org.hl7.fhir.dstu3.model.ContactPoint;
import org.hl7.fhir.dstu3.model.Enumerations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

/**
 * REST controller for managing Patient.
 */
@RestController
@RequestMapping("/api/fhir")
public class PatientFhirResource {
    private final Logger log = LoggerFactory.getLogger(PatientFhirResource.class);

    private static final String ENTITY_NAME = "patient";

    private final PatientService patientService;

    public PatientFhirResource(PatientService patientService, PatientQueryService patientQueryService) {
        this.patientService = patientService;
    }


    /**
     * GET  /patients/:id : get the "id" patient in FHIR format.
     *
     * @param id the id of the patient to retrieve
     * @return a string of the patient information in FHIR format
     */
    @GetMapping("/Patient/{id}")
    @Timed
    public String getPatient(@PathVariable Long id) {
        log.debug("REST request to get Patient in FHIR format: {}", id);
        if (id > patientService.getSize()){return "[]";}

        Patient patient = patientService.findOne(id);
        org.hl7.fhir.dstu3.model.Patient patientFhir = new org.hl7.fhir.dstu3.model.Patient();
        // add name
        patientFhir.addName().setFamily(patient.getFamilyName()).addGiven(patient.getGivenName());
        // add dob
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime btd = patient.getBirthDate().atStartOfDay(zoneId);
        patientFhir.setBirthDate(Date.from(btd.toInstant()));
        // add Email
        patientFhir.addTelecom().setSystem(ContactPoint.ContactPointSystem.EMAIL).setValue(patient.getEmail());
        patientFhir.addIdentifier().setSystem("ID").setValue(patient.getId().toString());


        // add NHS number
        if (patient.getNhsNumber() == null){
            patientFhir.addIdentifier().setSystem("nhsNumber").setValue("0000000000");
        }else{
            patientFhir.addIdentifier().setSystem("nhsNumber").setValue(patient.getNhsNumber().toString());
        }

        //add gender
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
        ctx.newJsonParser();
        p.setPrettyPrint(false);
        String encode = p.encodeResourceToString(patientFhir);
        return encode;
    }


    /**
     * GET  /patients : get all the patients in FHIR format.
     *
     * @param pageable the pagination information
     * @return a string with all patients information in FHIR format
     */
    @GetMapping("/Patient/all")
    @Timed
    public String getAllPatient(Pageable pageable) {
        log.debug("REST request to get Patients in FHIR format by criteria: {}");
        Page<Patient> page = patientService.findAll(pageable);

        // here we create a long String containing all patients' info in fhir standard, json format
        String patients = "[";
        int i, patientCount;
        patientCount = page.getContent().size();
//        patientCount = (int)patientService.getSize();
        if(patientCount == 0){ return "[]"; }
        for (i = 0; i < patientCount - 1; i++) {
            patients = patients + getPatient(page.getContent().get(i).getId()) + ",";
        }
        patients = patients + getPatient(page.getContent().get(i).getId()) + "]" + patientCount;

        return patients;
    }


    /**
     * SEARCH  /Patient?query=:query : search for the patient corresponding
     * to the query.
     * example: /Patient?query=1000000000 : search for patient with nhs number
     * 1000000000
     * query can be name or nhsNumber
     *
     * @param query the query of the patient search
     * @param pageable the pagination information
     * @return the result of the search in FHIR
     */
    @GetMapping("/Patient")
    @Timed
    public String searchPatients(@RequestParam String query, Pageable pageable) {
        log.debug("REST request to search for a page of Patients in FHIR format for query {}", query);
        Page<Patient> page = patientService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders
            (query, page, "/api/fhir/_search/patients");
        ResponseEntity<List<Patient>> responseEntity = new ResponseEntity<>
            (page.getContent(), headers, HttpStatus.OK);

        // identical to method above, but query only supports NHS number and patient's name
        String patients = "[";
        int i, patientCount;
        patientCount = responseEntity.getBody().size();
        if(patientCount == 0){ return "[]"; }
        for (i = 0; i < patientCount - 1; i++) {
            patients = patients + getPatient(responseEntity.getBody().get(i).getId()) + ",";
        }
        patients = patients + getPatient(responseEntity.getBody().get(i).getId()) + "]";

        return patients;
    }

}
