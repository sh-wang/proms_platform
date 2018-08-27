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
import com.noesisinformatica.northumbriaproms.domain.Procedure;
import com.noesisinformatica.northumbriaproms.service.ProcedureService;
import com.noesisinformatica.northumbriaproms.web.rest.util.PaginationUtil;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * REST controller for Resource QuestionnaireResponse.
 */
@RestController
@RequestMapping("/api/fhir")
public class ProcedureFhirResource {
    private final Logger log = LoggerFactory.getLogger(ProcedureFhirResource.class);

    private final ProcedureService procedureService;

    public ProcedureFhirResource(ProcedureService procedureService){
        this.procedureService = procedureService;
    }

    private FhirContext ctx = FhirContext.forDstu3();
    private IParser p =ctx.newJsonParser();

    /**
     * GET  /procedures/:id : get the "id" procedure.
     *
     * @param id the id of the procedure to retrieve
     * @return corresponding procedure in FHIR format
     */
    @GetMapping("/Procedure/{id}")
    @Timed
    public ResponseEntity<String> getProcedure(@PathVariable Long id) {
        log.debug("REST request to get Procedure in FHIR : {}", id);

        org.hl7.fhir.dstu3.model.Procedure procedureFhir = getProcedureResource(id);
        if (procedureFhir == null){return new ResponseEntity<>("[]", HttpStatus.OK);}

        //FHIR conversion
        String encode = p.encodeResourceToString(procedureFhir);
        return new ResponseEntity<>(encode, HttpStatus.OK);
    }


    /**
     * get the FHIR dstu3 procedure with ID id
     *
     * @param id the id of the procedure to retrieve
     * @return corresponding FHIR dstu3 procedure
     */
    public org.hl7.fhir.dstu3.model.Procedure getProcedureResource(Long id){
        org.hl7.fhir.dstu3.model.Procedure procedureFhir = new org.hl7.fhir.dstu3.model.Procedure();
        Procedure procedure = new Procedure();
        try{
            procedure = procedureService.findOne(id);
        }
        catch (Exception e){
            return null;
        }
//        if (procedure == null){ return null; }
        try{
            //add id
            procedureFhir.setId(id.toString());
            //currently no status data
            procedureFhir.setStatus(org.hl7.fhir.dstu3.model.Procedure.ProcedureStatus.UNKNOWN);

            CodeableConcept codeableConcept = new CodeableConcept();
            // add localcode as code and add name
            codeableConcept.addCoding().setCode(procedure.getLocalCode().toString()).
                setDisplay(procedure.getName().substring(1, procedure.getName().length()));
            procedureFhir.setCode(codeableConcept);
        }
        catch (Exception e){
            e.printStackTrace();
        }


        return procedureFhir;
    }


    /**
     * GET  /procedures : get all the procedures.
     *
     * @param pageable the pagination information
     * @return all procedures
     */
    @GetMapping("/Procedure/all")
    @Timed
    public ResponseEntity<String> getAllProcedures(Pageable pageable) {
        log.debug("REST request to get a page of Procedures in FHIR");
        Page<Procedure> page = procedureService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders
            (page, "/api/fhir/Procedure/all");
        if (page.getTotalElements() == 0){ return new ResponseEntity<>("[]", headers, HttpStatus.OK); }

        List<Procedure> procedureList = new ArrayList<>();
        int pageNumber = page.getTotalPages();
        while(pageNumber > 0){
            procedureList.addAll(page.getContent());
            page = procedureService.findAll(page.nextPageable());
            pageNumber--;
        }

        JsonArray procedureArray = new JsonArray();
        procedureArray = JsonConversion(procedureList, procedureArray);

        return new ResponseEntity<>(procedureArray.toString(), headers, HttpStatus.OK);
    }


    /**
     * Convert a list of FHIR patient information into a Json array
     *
     * @param actionList a list of procedure
     * @param procedureArray a blank Json array
     * @return the Json array contains all procedures information
     */
    private JsonArray JsonConversion(List<Procedure> actionList, JsonArray procedureArray){
        String questionnaireResponseString;
        JsonObject procedureJson;
        for(Procedure patient: actionList) {
            questionnaireResponseString = getProcedure(patient.getId()).getBody();
            com.google.gson.JsonParser toJson = new com.google.gson.JsonParser();
            procedureJson = toJson.parse(questionnaireResponseString).getAsJsonObject();
            procedureArray.add(procedureJson);
        }
        return procedureArray;
    }
}
