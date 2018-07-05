package com.noesisinformatica.northumbriaproms.web.rest;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.codahale.metrics.annotation.Timed;
import com.noesisinformatica.northumbriaproms.domain.Questionnaire;
import com.noesisinformatica.northumbriaproms.service.QuestionnaireService;
import org.hl7.fhir.dstu3.model.Enumerations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * REST controller for Resource QuestionnaireResponse.
 */
@RestController
@RequestMapping("/api/fhir")
public class QuestionnaireFhirResource {
    private final Logger log = LoggerFactory.getLogger(QuestionnaireFhirResource.class);
    private final QuestionnaireService questionnaireService;

    public QuestionnaireFhirResource(QuestionnaireService questionnaireService){
        this.questionnaireService = questionnaireService;
    }

    /**
     * GET  /questionnaires/:id : get the "id" questionnaire.
     *
     * @param id the id of the questionnaire to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the questionnaire, or with status 404 (Not Found)
     */
    @GetMapping("/questionnaires/{id}")
    @Timed
    public String getQuestionnaire(@PathVariable Long id) {
        log.debug("REST request to get Questionnaire : {}", id);
        Questionnaire questionnaire = questionnaireService.findOne(id);
        org.hl7.fhir.dstu3.model.Questionnaire questionnaireFhir = new org.hl7.fhir.dstu3.model.Questionnaire();
        questionnaireFhir.setUrl("localhost:8080/api/fhir/questionnaire/"+id);
        questionnaireFhir.setId(id.toString());
        questionnaireFhir.setStatus(Enumerations.PublicationStatus.ACTIVE);
        questionnaireFhir.setName(questionnaire.getName());
        questionnaireFhir.setCopyright(questionnaire.getCopyright());

        FhirContext ctx = FhirContext.forDstu3();
        IParser p =ctx.newJsonParser();
        p.setPrettyPrint(true);
        String encode = p.encodeResourceToString(questionnaireFhir);
        return encode;
    }

}
