package com.noesisinformatica.northumbriaproms.service.impl;

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

import com.noesisinformatica.northumbriaproms.domain.Patient;
import com.noesisinformatica.northumbriaproms.domain.enumeration.GenderType;
import com.noesisinformatica.northumbriaproms.repository.PatientRepository;
import com.noesisinformatica.northumbriaproms.repository.search.PatientSearchRepository;
import com.noesisinformatica.northumbriaproms.service.PatientService;
import com.noesisinformatica.northumbriaproms.web.rest.util.PatientQueryModel;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * Service Implementation for managing Patient.
 */
@Service
@Transactional
public class PatientServiceImpl implements PatientService{

    private final Logger log = LoggerFactory.getLogger(PatientServiceImpl.class);

    private final PatientRepository patientRepository;

    private final PatientSearchRepository patientSearchRepository;

    public PatientServiceImpl(PatientRepository patientRepository, PatientSearchRepository patientSearchRepository) {
        this.patientRepository = patientRepository;
        this.patientSearchRepository = patientSearchRepository;
    }

    /**
     * Save a patient.
     *
     * @param patient the entity to save
     * @return the persisted entity
     */
    @Override
    public Patient save(Patient patient) {
        log.debug("Request to save Patient : {}", patient);
        Patient result = patientRepository.save(patient);
        patientSearchRepository.save(result);
        return result;
    }

    /**
     * Get all the patients.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Patient> findAll(Pageable pageable) {
        log.debug("Request to get all Patients");
        return patientRepository.findAll(pageable);
    }

    /**
     * Get one patient by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public Patient findOne(Long id) {
        log.debug("Request to get Patient : {}", id);
        return patientRepository.findOne(id);
    }

    /**
     * Delete the patient by id.
     *
     * @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete Patient : {}", id);
        patientRepository.delete(id);
        patientSearchRepository.delete(id);
    }

    /**
     * Search for the patient corresponding to the query.
     *
     * @param query the query of the search
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Patient> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Patients for query {}", query);
        QueryBuilder queryBuilder = null;
        // try to see if query is number, if it is try as nhs number otherwise try as name
        try {
            Long number = Long.parseLong(query);
            queryBuilder = QueryBuilders.termQuery("nhsNumber", number);
        } catch (NumberFormatException e) {
            queryBuilder =
                QueryBuilders.multiMatchQuery(query, "givenName", "familyName").type(MultiMatchQueryBuilder.Type.PHRASE_PREFIX);
        }
        Page<Patient> result = patientSearchRepository.search(queryBuilder, pageable);
        return result;
    }


    /**
     * Search for the patient corresponding to the query.
     *
     * @param query the query of the search
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Patient> searchFHIR(PatientQueryModel query, Pageable pageable) {
        log.debug("Request to search for a page of Patient for query {}", query);

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        BoolQueryBuilder identifierQueryBuilder = QueryBuilders.boolQuery();
        for(Long identifier : query.getNhsNumber()) {
            identifierQueryBuilder.should(QueryBuilders.matchQuery("nhsNumber", identifier));
        }

        BoolQueryBuilder nameQueryBuilder = QueryBuilders.boolQuery();
        for(String name : query.getName()) {
            nameQueryBuilder.should(QueryBuilders.multiMatchQuery(name, "familyName", "givenName"));
        }

        BoolQueryBuilder givenQueryBuilder = QueryBuilders.boolQuery();
        for(String given : query.getGivenName()){
            givenQueryBuilder.should(QueryBuilders.matchQuery("givenName", given));
        }

        BoolQueryBuilder familyQueryBuilder = QueryBuilders.boolQuery();
        for(String family : query.getFamilyName()){
            familyQueryBuilder.should(QueryBuilders.matchQuery("familyName", family));
        }

        BoolQueryBuilder emailQueryBuilder = QueryBuilders.boolQuery();
        for(String email : query.getEmail()){
            emailQueryBuilder.should(QueryBuilders.matchQuery("email", email));
        }

        BoolQueryBuilder genderQueryBuilder = QueryBuilders.boolQuery();
        for(GenderType gender : query.getGender()){
            genderQueryBuilder.should(QueryBuilders.matchQuery("gender", gender));
        }

        BoolQueryBuilder birthQueryBuilder = QueryBuilders.boolQuery();
        for(Date birth : query.getBirthDate()) {
            LocalDate localbirth = birth.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            birthQueryBuilder.should(QueryBuilders.matchQuery("birthDate", localbirth));
        }


        // we only add Identifier clause if there are 1 or more Identifier specified
        if(query.getNhsNumber().size() > 0){
            boolQueryBuilder.must(identifierQueryBuilder);
        }

        // we only add name clause if there are 1 or more Authored specified
        if(query.getName().size() > 0){
            boolQueryBuilder.must(nameQueryBuilder);
        }

        // we only add given name clause if there are 1 or more Parent specified
        if(query.getGivenName().size() > 0){
            boolQueryBuilder.must(givenQueryBuilder);
        }

        // we only add family name clause if there are 1 or more Questionnaire specified
        if (query.getFamilyName().size() > 0){
            boolQueryBuilder.must(familyQueryBuilder);
        }

        // we only add email clause if there are 1 or more Status specified
        if (query.getEmail().size() > 0){
            boolQueryBuilder.must(emailQueryBuilder);
        }

        // we only add gender clause if there are 1 or more Patient specified
        if (query.getGender().size() > 0){
            boolQueryBuilder.must(genderQueryBuilder);
        }

        // we only add birthday clause if there are 1 or more Subject specified
        if (query.getBirthDate().size() > 0){
            boolQueryBuilder.must(birthQueryBuilder);
        }

        log.debug("boolQueryBuilder = " + boolQueryBuilder);
        // build and return boolean query
        System.out.println(boolQueryBuilder);

        return patientSearchRepository.search(boolQueryBuilder,pageable);
    }

    //    @Override
//    @Transactional(readOnly = true)
//    public Page<Patient> searchFHIR(Map query, Pageable pageable) {
//        log.debug("Request to search for a page of Patients for query {}", query);
//        QueryBuilder queryBuilder = null;
//
////        String address_postalcode = query.get("address_postalcode").toString();
////        Long phone = Long.parseLong(query.get("phone").toString());  no phone in patient entity
//
//        if(query.get("family")!= null){
//            queryBuilder = QueryBuilders.termQuery("familyName", query.get("family"));
//            System.out.println(query.get("family"));
//        }
//        if(query.get("identifier")!=null){
//            queryBuilder = QueryBuilders.termQuery("id", query.get("identifier"));
//        }
//        if (query.get("email")!=null){
//            queryBuilder = QueryBuilders.termQuery("email", query.get("email"));
//        }
//        if (query.get("given")!=null){
//            queryBuilder = QueryBuilders.termQuery("givenName", query.get("given"));
//        }
//        if (query.get("name")!=null){
//            queryBuilder = QueryBuilders.multiMatchQuery(query.get("name"), "givenName", "familyName").type(MultiMatchQueryBuilder.Type.PHRASE_PREFIX);
//        }
//        if (query.get("birthdate")!=null){
//            queryBuilder = QueryBuilders.termQuery("birthDate", query.get("birthdate"));
//        }
//        if (query.get("gender")!=null){
//            queryBuilder = QueryBuilders.termQuery("gender", query.get("gender"));
//        }
////        if (query.get("address_postalcode")!= null){
////            Page<Address> address = addressService.search(query.get("address_postalcode").toString(),pageable);
////            Page<Patient> result;
////            while(address.hasNext()){
////                result.
////            }
////        }
//
////        // try to see if query is number, if it is try as nhs number otherwise try as name
////        try {
////            Long number = Long.parseLong(query);
////            queryBuilder = QueryBuilders.termQuery("nhsNumber", number);
////        } catch (NumberFormatException e) {
////            queryBuilder =
////                QueryBuilders.multiMatchQuery(query, "givenName", "familyName").type(MultiMatchQueryBuilder.Type.PHRASE_PREFIX);
////        }
//        Page<Patient> result = patientSearchRepository.search(queryBuilder, pageable);
//        System.out.println(queryBuilder);
//        return result;
//    }
}
