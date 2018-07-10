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
import com.noesisinformatica.northumbriaproms.domain.Procedure;
import com.noesisinformatica.northumbriaproms.service.ProcedureService;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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


    /**
     * GET  /procedures/:id : get the "id" procedure.
     *
     * @param id the id of the procedure to retrieve
     * @return corresponding procedure in FHIR format
     */
    @GetMapping("/Procedure/{id}")
    @Timed
    public String getProcedure(@PathVariable Long id) {
        log.debug("REST request to get Procedure in FHIR : {}", id);

        org.hl7.fhir.dstu3.model.Procedure procedureFhir = getProcedureResource(id);
        if (procedureFhir == null){return "[]";}

        //FHIR conversion
        FhirContext ctx = FhirContext.forDstu3();
        IParser p =ctx.newJsonParser();
        p.setPrettyPrint(false);
        String encode = p.encodeResourceToString(procedureFhir);
        return encode;
    }


    /**
     * get the FHIR dstu3 procedure with ID id
     *
     * @param id the id of the procedure to retrieve
     * @return corresponding FHIR dstu3 procedure
     */
    public org.hl7.fhir.dstu3.model.Procedure getProcedureResource(Long id){
        Procedure procedure = procedureService.findOne(id);
        if (procedure == null){ return null; }
        org.hl7.fhir.dstu3.model.Procedure procedureFhir = new org.hl7.fhir.dstu3.model.Procedure();

        procedureFhir.setId(id.toString());
        //currently no status data
        procedureFhir.setStatus(org.hl7.fhir.dstu3.model.Procedure.ProcedureStatus.UNKNOWN);

        CodeableConcept codeableConcept = new CodeableConcept();
        codeableConcept.addCoding().setCode(procedure.getLocalCode().toString()).
            setDisplay(procedure.getName().substring(1, procedure.getName().length()));
        procedureFhir.setCode(codeableConcept);

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
    public String getAllProcedures(Pageable pageable) {
        log.debug("REST request to get a page of Procedures in FHIR");
        Page<Procedure> page = procedureService.findAll(pageable);

        String procedures = "[";
        long i, procedureCount;
        procedureCount = page.getTotalElements();
        if(procedureCount == 0){ return "[]"; }
        for (i = 1; i < procedureCount; i++) {
            procedures = procedures + getProcedure(i) + ",";
        }

        procedures = procedures + getProcedure(i) + "]";
        return procedures;
    }
}
