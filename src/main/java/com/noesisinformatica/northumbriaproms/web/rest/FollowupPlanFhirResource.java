package com.noesisinformatica.northumbriaproms.web.rest;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.codahale.metrics.annotation.Timed;
import com.noesisinformatica.northumbriaproms.domain.FollowupPlan;
import com.noesisinformatica.northumbriaproms.domain.Patient;
import com.noesisinformatica.northumbriaproms.service.FollowupPlanService;
import io.github.jhipster.web.util.ResponseUtil;
import org.hl7.fhir.dstu3.model.CarePlan;
import org.hl7.fhir.dstu3.model.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/fhir")
public class FollowupPlanFhirResource {

    private final Logger log = LoggerFactory.getLogger(FollowupPlanResource.class);

    private static final String ENTITY_NAME = "followupPlan";

    private final FollowupPlanService followupPlanService;

    public FollowupPlanFhirResource(FollowupPlanService followupPlanService) {
        this.followupPlanService = followupPlanService;
    }



    // THIS CLASS IS CURRENTLY OUT OF SERVICE.
    // we try to use followupPlan to add "baseon" for questionnaire-response resource, but it doesn't mapped well



    /**
     * GET  /procedure-bookings/:id/followup-plan : get the followupPlan for "id" procedure booking
     *
     * @param id the id of the procedureBooking to retrieve followupPlan for
     * @return the ResponseEntity with status 200 (OK) and with body the followupPlan, or with status 404 (Not Found)
     */
    @GetMapping("/followupPlan/{id}")
    @Timed
    public String getFollowupPlanForBookingId(@PathVariable Long id) {
        log.debug("REST request to get FollowupPlan for ProcedureBooking : {}", id);
        Optional<FollowupPlan> followupPlan = followupPlanService.findOneByProcedureBookingId(id);
//        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(followupPlan.get()));
        CarePlan carePlan = new CarePlan();

        carePlan.setId(followupPlan.get().getId().toString());
        carePlan.setStatus(CarePlan.CarePlanStatus.ACTIVE);
        carePlan.setIntent(CarePlan.CarePlanIntent.PLAN);

        Patient patient = followupPlan.get().getPatient();
        Reference patientReference = new Reference();
        patientReference.setReference("localhost:8080/api/fhir/patients/"+patient.getId());
        carePlan.setSubject(patientReference);

//        Reference careteamReference = new Reference();
//        careteamReference

        FhirContext ctx = FhirContext.forDstu3();
        IParser p =ctx.newJsonParser();
        p.setPrettyPrint(true);
        String encode = p.encodeResourceToString(carePlan);
        return encode;



    }


}
