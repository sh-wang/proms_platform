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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.noesisinformatica.northumbriaproms.domain.Address;
import com.noesisinformatica.northumbriaproms.domain.FollowupAction;
import com.noesisinformatica.northumbriaproms.domain.Patient;
import com.noesisinformatica.northumbriaproms.domain.enumeration.GenderType;
import com.noesisinformatica.northumbriaproms.service.AddressService;
import com.noesisinformatica.northumbriaproms.service.PatientQueryService;
import com.noesisinformatica.northumbriaproms.service.PatientService;
import com.noesisinformatica.northumbriaproms.service.dto.PatientCriteria;
import com.noesisinformatica.northumbriaproms.web.rest.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.hl7.fhir.dstu3.model.ContactPoint;
import org.hl7.fhir.dstu3.model.Enumerations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * REST controller for managing Patient.
 */
@RestController
@RequestMapping("/api/fhir")
public class PatientFhirResource {
    private final Logger log = LoggerFactory.getLogger(PatientFhirResource.class);

//    private static final String ENTITY_NAME = "patient";

    private final PatientService patientService;
    private final AddressService addressService;
    private final PatientQueryService patientQueryService;

    public PatientFhirResource(PatientService patientService,
                               AddressService addressService,
                               PatientQueryService patientQueryService) {
        this.patientService = patientService;
        this.addressService = addressService;
        this.patientQueryService = patientQueryService;
    }

    private FhirContext ctx = FhirContext.forDstu3();
    private IParser p =ctx.newJsonParser();

    /**
     * GET  /patients/:id : get the "id" patient in FHIR format.
     *
     * @param id the id of the patient to retrieve
     * @return a string of the patient information in FHIR format
     */
    @GetMapping("/Patient/{id}")
    @Timed
    public ResponseEntity<String> getPatient(@PathVariable Long id) {
        log.debug("REST request to get Patient in FHIR format: {}", id);

        org.hl7.fhir.dstu3.model.Patient patientFhir = getPatientResource(id);
        if (patientFhir == null){ return new ResponseEntity<>("[]", HttpStatus.OK); }

        //FHIR conversion
        String encode = p.encodeResourceToString(patientFhir);
        return new ResponseEntity<>(encode, HttpStatus.OK);
    }


    /**
     * Get the FHIR dust3 patient with ID id
     *
     * @param id the id of the patient to retrieve
     * @return the patient
     */
    public org.hl7.fhir.dstu3.model.Patient getPatientResource(Long id) {
        Patient patient = patientService.findOne(id);
        if (patient == null){ return null;}
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

        // add Address
        Address address = addressService.findOne(id);
        if (address != null){
            org.hl7.fhir.dstu3.model.Address addressFHIR = new org.hl7.fhir.dstu3.model.Address();
            addressFHIR.setPostalCode(address.getPostalCode());
            addressFHIR.setCity(address.getCity());
            addressFHIR.setCountry(address.getCountry());

            for(String line: address.getLines()){
                addressFHIR.addLine(line);
            }

            patientFhir.addAddress(addressFHIR);
        }

        return patientFhir;
    }



    /**
     * GET  /patients : get all the patients in FHIR format.
     *
     * @param pageable the pagination information
     * @return a string with all patients information in FHIR format
     */
    @GetMapping("/Patient/all")
    @Timed
    public ResponseEntity<String> getAllPatient(Pageable pageable) {
        log.debug("REST request to get Patients in FHIR format by criteria: {}");
        Page<Patient> page = patientService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/fhir/Patient/all");
        if (page.getTotalElements() == 0){ return new ResponseEntity<>("[]", headers, HttpStatus.OK); }

        JsonArray patientArray = JsonConversion(page);

        return new ResponseEntity<>(patientArray.toString(), headers, HttpStatus.OK);
    }


    /**
     * Convert a list of FHIR patient information into a Json array
     *
     * @return the Json array contains all patients information
     */
    private JsonArray JsonConversion(Page page){
        List<Patient> patientList = new ArrayList<>();
        String questionnaireResponseString;
        JsonObject patientJson;
        JsonArray patientArray = new JsonArray();
        int pageNumber = page.getTotalPages();

        while(pageNumber > 0){
            patientList.addAll(page.getContent());
            page = patientService.findAll(page.nextPageable());
            pageNumber--;
        }

        for(Patient patient: patientList) {
            questionnaireResponseString = getPatient(patient.getId()).getBody();
            com.google.gson.JsonParser toJson = new com.google.gson.JsonParser();
            patientJson = toJson.parse(questionnaireResponseString).getAsJsonObject();
            patientArray.add(patientJson);
        }
        return patientArray;
    }


//    /**
//     * SEARCH  /Patient?query=:query : search for the patient corresponding
//     * to the query.
//     * example: /Patient?query=1000000000 : search for patient with nhs number
//     * 1000000000
//     * query can be name or nhsNumber
//     *
//     * @param pageable the pagination information
//     * @return the result of the search in FHIR
//     */
    @GetMapping("/Patient")
    @Timed
    public ResponseEntity<String> searchPatients(String postcode, String family, Long id, Pageable pageable) {
        Map query = new HashMap();
        query.put("address-postalcode", postcode);
        query.put("family", family);
        query.put("id", id);
        log.debug("REST request to search for a page of Patients in FHIR format for query {}", query);
        Page<Patient> page = patientService.searchFHIR(query, pageable);
//        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders
//            (query.get("family").toString(), page, "/api/fhir/Patient");

        List<Patient> patientList = page.getContent();
        if(patientList!=null){
            System.out.println(patientList.size());
        }
        JsonArray patArray = new JsonArray();

        for(Patient pat: patientList){
            String patInfo = getPatient(pat.getId()).getBody();
            com.google.gson.JsonParser toJson = new JsonParser();
            JsonObject patJson = toJson.parse(patInfo).getAsJsonObject();
            patArray.add(patJson);
        }
        return new ResponseEntity<>(patArray.toString(), HttpStatus.OK);


    }


//    @GetMapping("/Patients")
//    @Timed
//    public ResponseEntity<String> searchPatient(PatientCriteria criteria, Pageable pageable) {
//        log.debug("REST request to get Patients by criteria: {}", criteria);
//        Page<Patient> page = patientQueryService.findByCriteria(criteria, pageable);
//        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/fhir/Patients");
//        if (page.getTotalElements() == 0){ return new ResponseEntity<>("[]", headers, HttpStatus.OK); }
//
//        JsonArray patientArray = JsonConversion(page);
//
//        return new ResponseEntity<>(patientArray.toString(), headers, HttpStatus.OK);
//    }


     /**
     * SEARCH  /Patient?query=:query : search for the patient corresponding
     * to the query.
     * example: /Patient?query=1000000000 : search for patient with nhs number
     * 1000000000
     * query can be name or nhsNumber
     * @param query the query content
     * @param pageable the pagination information
     * @return the result of the search in FHIR
     */
//    @GetMapping("/Patient")
//    @Timed
//    public ResponseEntity<String> searchPatients(@RequestParam String query, Pageable pageable) {
//        log.debug("REST request to search for a page of Patients in FHIR format for query {}", query);
//        Page<Patient> page = patientService.search(query, pageable);
//        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders
//            (query, page, "/api/fhir/Patient");
//
//        List<Patient> patientList = page.getContent();
//        JsonArray patArray = new JsonArray();
//
//        for(Patient pat: patientList){
//            String patInfo = getPatient(pat.getId());
//            com.google.gson.JsonParser toJson = new JsonParser();
//            JsonObject patJson = toJson.parse(patInfo).getAsJsonObject();
//            patArray.add(patJson);
//        }
//        return new ResponseEntity<>(patArray.toString(), headers, HttpStatus.OK);
//
//
//    }
}
