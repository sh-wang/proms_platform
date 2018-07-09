package com.noesisinformatica.northumbriaproms.web.rest;

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
        int i, procedureCount;
        procedureCount = page.getContent().size();
        if(procedureCount == 0){ return "[]"; }
        for (i = 0; i < procedureCount - 1; i++) {
            procedures = procedures + getProcedure(page.getContent().get(i).getId()) + ",";
        }

        procedures = procedures + getProcedure(page.getContent().get(i).getId()) + "]";
        return procedures;
    }
}
