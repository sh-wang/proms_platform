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

import com.codahale.metrics.annotation.Timed;
import com.noesisinformatica.northumbriaproms.domain.Patient;
import com.noesisinformatica.northumbriaproms.domain.enumeration.GenderType;
import com.noesisinformatica.northumbriaproms.service.PatientService;
import com.noesisinformatica.northumbriaproms.web.rest.errors.BadRequestAlertException;
import com.noesisinformatica.northumbriaproms.web.rest.util.HeaderUtil;
import com.noesisinformatica.northumbriaproms.web.rest.util.PaginationUtil;
import com.noesisinformatica.northumbriaproms.service.dto.PatientCriteria;
import com.noesisinformatica.northumbriaproms.service.PatientQueryService;
import com.noesisinformatica.northumbriaproms.web.rest.util.PatientQueryModel;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * REST controller for managing Patient.
 */
@RestController
@RequestMapping("/api")
public class PatientResource {

    private final Logger log = LoggerFactory.getLogger(PatientResource.class);

    private static final String ENTITY_NAME = "patient";

    private final PatientService patientService;

    private final PatientQueryService patientQueryService;

    public PatientResource(PatientService patientService, PatientQueryService patientQueryService) {
        this.patientService = patientService;
        this.patientQueryService = patientQueryService;
    }

    /**
     * POST  /patients : Create a new patient.
     *
     * @param patient the patient to create
     * @return the ResponseEntity with status 201 (Created) and with body the new patient, or with status 400 (Bad Request) if the patient has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/patients")
    @Timed
    public ResponseEntity<Patient> createPatient(@Valid @RequestBody Patient patient) throws URISyntaxException {
        log.debug("REST request to save Patient : {}", patient);
        if (patient.getId() != null) {
            throw new BadRequestAlertException("A new patient cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Patient result = patientService.save(patient);
        return ResponseEntity.created(new URI("/api/patients/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /patients : Updates an existing patient.
     *
     * @param patient the patient to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated patient,
     * or with status 400 (Bad Request) if the patient is not valid,
     * or with status 500 (Internal Server Error) if the patient couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/patients")
    @Timed
    public ResponseEntity<Patient> updatePatient(@Valid @RequestBody Patient patient) throws URISyntaxException {
        log.debug("REST request to update Patient : {}", patient);
        if (patient.getId() == null) {
            return createPatient(patient);
        }
        Patient result = patientService.save(patient);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, patient.getId().toString()))
            .body(result);
    }

    /**
     * GET  /patients : get all the patients.
     *
     * @param pageable the pagination information
     * @param criteria the criterias which the requested entities should match
     * @return the ResponseEntity with status 200 (OK) and the list of patients in body
     */
    @GetMapping("/patients")
    @Timed
    public ResponseEntity<List<Patient>> getAllPatients(PatientCriteria criteria, Pageable pageable) {
        log.debug("REST request to get Patients by criteria: {}", criteria);
        Page<Patient> page = patientQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/patients");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /patients/:id : get the "id" patient.
     *
     * @param id the id of the patient to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the patient, or with status 404 (Not Found)
     */
    @GetMapping("/patients/{id}")
    @Timed
    public ResponseEntity<Patient> getPatient(@PathVariable Long id) {
        log.debug("REST request to get Patient : {}", id);
        Patient patient = patientService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(patient));
    }

    /**
     * DELETE  /patients/:id : delete the "id" patient.
     *
     * @param id the id of the patient to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/patients/{id}")
    @Timed
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        log.debug("REST request to delete Patient : {}", id);
        patientService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    /**
     * SEARCH  /_search/patients?query=:query : search for the patient corresponding
     * to the query.
     *
     * @param query the query of the patient search
     * @param pageable the pagination information
     * @return the result of the search
     */
    @GetMapping("/_search/patients")
    @Timed
    public ResponseEntity<List<Patient>> searchPatients(@RequestParam String query, Pageable pageable) {
        log.debug("REST request to search for a page of Patients for query {}", query);
        Page<Patient> page = patientService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/patients");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }


    /**
     * search url example:
     * api/patients?gender=male&name=abc
     *
     * @param address_postalcode the query of the patient search
     * @param birthdate the query of the patient search
     * @param family the query of the patient search
     * @param email the query of the patient search
     * @param gender the query of the patient search
     * @param given the query of the patient search
     * @param name the query of the patient search
     * @param identifier the query of the patient search
     * @param pageable the pagination information
     * @return the result of the search
     */


    @GetMapping("/search/patients")
    @Timed
    public ResponseEntity<List<Patient>> searchPatients(String address_postalcode,
                                                 String birthdate,String family,
                                                 String email, String gender,
                                                 String given, String name,
                                                 String identifier,
                                                 @PageableDefault(sort = {"id"},
                                                     direction = Sort.Direction.ASC) Pageable pageable) {
        if(address_postalcode==null && birthdate==null && family==null && email==null && gender==null
            && given==null && name==null && identifier==null){
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
        }
        PatientQueryModel patientQueryModel = new PatientQueryModel();

        List<String> emptyValue = new ArrayList();
        List<GenderType>genderEmpty = new ArrayList<>();
        List<Date> dateEmpty = new ArrayList<>();
        List<Long> idEmpty = new ArrayList<>();

        if (address_postalcode != null){
            patientQueryModel.setAddress(emptyValue);
        }else{
            patientQueryModel.setAddress(emptyValue);
        }

        if (birthdate != null){
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date date = format.parse(birthdate);
                patientQueryModel.setBirthDate(Collections.singletonList(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }else {
            patientQueryModel.setBirthDate(dateEmpty);
        }

        if (family != null){
            patientQueryModel.setFamilyName(Collections.singletonList(family));
        }else {
            patientQueryModel.setFamilyName(emptyValue);
        }

        if (email != null){
            patientQueryModel.setEmail(Collections.singletonList(email));
        }else {
            patientQueryModel.setEmail(emptyValue);
        }

        if (gender != null){
            switch (gender){
                case "male":
                    genderEmpty.add(GenderType.MALE);
                    break;
                case "female":
                    genderEmpty.add(GenderType.FEMALE);
                    break;
                case "unknown":
                    genderEmpty.add(GenderType.UNKNOWN);
                    break;
                case "other":
                    genderEmpty.add(GenderType.OTHER);
                    break;
                default:
                    genderEmpty.add(GenderType.FORSEARCH);
                    break;
            }
            patientQueryModel.setGender(genderEmpty);
        }else {
            patientQueryModel.setGender(genderEmpty);
        }

        if (given != null){
            patientQueryModel.setGivenName(Collections.singletonList(given));
        } else {
            patientQueryModel.setGivenName(emptyValue);
        }

        if (name != null){
            patientQueryModel.setName(Collections.singletonList(name));
        } else {
            patientQueryModel.setName(emptyValue);
        }

        if (identifier != null){
            try {
                idEmpty.add(Long.parseLong(identifier));
            }catch (Exception e){ }
            patientQueryModel.setNhsNumber(idEmpty);
        } else {
            patientQueryModel.setNhsNumber(idEmpty);
        }

        log.debug("REST request to search for a page of Patient for query {}", patientQueryModel);
        Page<Patient> page = patientService.searchFHIR(patientQueryModel, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(patientQueryModel.toString(),
            page, "/api/patientss");
        if (page.getTotalElements() == 0){ return new ResponseEntity<>(new ArrayList<>(), headers, HttpStatus.OK); }

        // wrap results page in a response entity with faceted results turned into a map
        List<Patient> patientList = new ArrayList<>();
        int pageNumber = page.getTotalPages();

        while(pageNumber > 0){
            patientList.addAll(page.getContent());
            page = patientService.findAll(page.nextPageable());
            pageNumber--;
        }

        return new ResponseEntity<>(patientList, headers, HttpStatus.OK);
    }

}
