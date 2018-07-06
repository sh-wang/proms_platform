package com.noesisinformatica.northumbriaproms.web.rest;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.codahale.metrics.annotation.Timed;
import com.noesisinformatica.northumbriaproms.domain.Procedure;
import com.noesisinformatica.northumbriaproms.service.ProcedureService;
import io.github.jhipster.web.util.ResponseUtil;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

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
     * @return corresponding proceture in FHIR format
     */
    @GetMapping("/procedures/{id}")
    @Timed
    public String getProcedure(@PathVariable Long id) {
        log.debug("REST request to get Procedure : {}", id);
        Procedure procedure = procedureService.findOne(id);

        org.hl7.fhir.dstu3.model.Procedure procedureFhir = new org.hl7.fhir.dstu3.model.Procedure();
        procedureFhir.setId(id.toString());
        //currently no status data
        procedureFhir.setStatus(org.hl7.fhir.dstu3.model.Procedure.ProcedureStatus.UNKNOWN);

        CodeableConcept codeableConcept = new CodeableConcept();
        codeableConcept.addCoding().setCode(procedure.getLocalCode().toString()).
            setDisplay(procedure.getName().substring(1, procedure.getName().length()));
        procedureFhir.setCode(codeableConcept);

        FhirContext ctx = FhirContext.forDstu3();
        IParser p =ctx.newJsonParser();
        p.setPrettyPrint(true);
        String encode = p.encodeResourceToString(procedureFhir);
        return encode;
    }

}
