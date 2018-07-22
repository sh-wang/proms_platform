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

import com.noesisinformatica.northumbriaproms.config.Constants;
import com.noesisinformatica.northumbriaproms.domain.CareEvent;
import com.noesisinformatica.northumbriaproms.domain.FollowupAction;
import com.noesisinformatica.northumbriaproms.domain.enumeration.ActionStatus;
import com.noesisinformatica.northumbriaproms.repository.FollowupActionRepository;
import com.noesisinformatica.northumbriaproms.repository.search.FollowupActionSearchRepository;
import com.noesisinformatica.northumbriaproms.service.FollowupActionService;
import com.noesisinformatica.northumbriaproms.web.rest.util.QueryModel;
import com.noesisinformatica.northumbriaproms.web.rest.util.QuestionnaireQueryModel;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.FacetedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

/**
 * Service Implementation for managing FollowupAction.
 */
@Service
@Transactional
//@EnableBinding(FollowupActionService.class)
//@RabbitListener(queues = Constants.ACTIONS_QUEUE)
//@RabbitListener(bindings = @QueueBinding(value = @Queue(value = Constants.ACTIONS_QUEUE, durable = "true") , exchange = @Exchange(value = "exch", autoDelete = "true") , key = "key") )
public class FollowupActionServiceImpl implements FollowupActionService {

    private final Logger log = LoggerFactory.getLogger(FollowupActionServiceImpl.class);

    private final FollowupActionRepository followupActionRepository;
    private final ElasticsearchTemplate elasticsearchTemplate;
    private final FollowupActionSearchRepository followupActionSearchRepository;

    public FollowupActionServiceImpl(FollowupActionRepository followupActionRepository,
                                     FollowupActionSearchRepository followupActionSearchRepository,
                                     ElasticsearchTemplate elasticsearchTemplate) {
        this.followupActionRepository = followupActionRepository;
        this.followupActionSearchRepository = followupActionSearchRepository;
        this.elasticsearchTemplate = elasticsearchTemplate;
    }

    /**
     * Save a followupAction.
     *
     * @param followupAction the entity to save
     * @return the persisted entity
     */
    @Override
    public FollowupAction save(FollowupAction followupAction) {
        log.debug("Request to save FollowupAction : {}", followupAction);
        FollowupAction result = followupActionRepository.save(followupAction);
        followupActionSearchRepository.save(result);
        return result;
    }

    /**
     * Process a followupAction.
     *
     * @param followupAction the entity to process
     */
    @Override
    @RabbitListener(queues = Constants.ACTIONS_QUEUE)
    public void processFollowupAction(FollowupAction followupAction) {
        log.debug("Request to process FollowupAction : {}", followupAction);
        this.save(followupAction);
    }

    /**
     * Process a {@link com.noesisinformatica.northumbriaproms.domain.CareEvent}.
     *
     * @param careEvent the entity to process
     */
    @Override
    @RabbitListener(queues = Constants.CARE_EVENTS_QUEUE)
    public void processCareEvent(CareEvent careEvent) {
        log.debug("Request to process CareEvent : {}", careEvent);
        careEvent.getFollowupActions().forEach(this::save);
    }

    /**
     * Get all the followupActions.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<FollowupAction> findAll(Pageable pageable) {
        log.debug("Request to get all FollowupActions");
        return followupActionRepository.findAll(pageable);
    }

    /**
     * Get one followupAction by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public FollowupAction findOne(Long id) {
        log.debug("Request to get FollowupAction : {}", id);
        return followupActionRepository.findOne(id);
    }

    /**
     * Delete the followupAction by id.
     *
     * @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete FollowupAction : {}", id);
        followupActionRepository.delete(id);
        followupActionSearchRepository.delete(id);
    }

    /**
     * Return all followup actions with all categories.
     *
     *  @return the page of followup actions
     */
    @Override
    @Transactional(readOnly = true)
    public FacetedPage<FollowupAction> findAllWithCategories(Pageable pageable) {
        log.debug("Request to search for a page of Trials for page {}", pageable);
        // build and return match all query
        return getFacetedPageForQuery(QueryBuilders.matchAllQuery(), pageable);
    }

    /**
     *  Index all the followup actions.
     *
     *  @return the boolean that represents the success of the index action
     */
    @Override
    @Transactional(readOnly = true)
    public boolean indexAll() {
        log.debug("Request to index followup actions");
        boolean result = false;

        // delete existing indices
        try {
            elasticsearchTemplate.deleteIndex(FollowupAction.class);
        } catch (IndexNotFoundException e) {
            log.error("Error deleting indices. Assuming index does not exist.");
        }

        try {
            followupActionRepository.findAll().forEach(followupActionSearchRepository::save);
            result = true;
        } catch (Exception e) {
            log.error("Error indexing followup actions .Nested exception is : ", e);
        }

        return result;
    }


    /**
     * Search for the followupAction corresponding to the query.
     *
     * @param query the query of the search
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public FacetedPage<FollowupAction> search(QueryModel query, Pageable pageable) {
        log.debug("Request to search for a page of Followup Actions for query {}", query);

        // if empty query, then just return all follow up actions
        if(query == null || query.isEmpty()) {
            // build and return match all query
            return getFacetedPageForQuery(QueryBuilders.matchAllQuery(), pageable);
        }

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        BoolQueryBuilder proceduresQueryBuilder = QueryBuilders.boolQuery();
        for(String condition : query.getProcedures()) {
            proceduresQueryBuilder.should(QueryBuilders.matchQuery("careEvent.followupPlan.procedureBooking.primaryProcedure", condition));
        }

        BoolQueryBuilder sourcesQueryBuilder = QueryBuilders.boolQuery();
        for(String location : query.getLocations()) {
            sourcesQueryBuilder.should(QueryBuilders.matchPhraseQuery("careEvent.followupPlan.procedureBooking.hospitalSite", location));
        }

        BoolQueryBuilder consultantsQueryBuilder = QueryBuilders.boolQuery();
        for(String consultant : query.getConsultants()) {
            consultantsQueryBuilder.should(QueryBuilders.matchPhraseQuery("careEvent.followupPlan.procedureBooking.consultantName", consultant));
        }

        BoolQueryBuilder patientIdsQueryBuilder = QueryBuilders.boolQuery();
        for(String id : query.getPatientIds()) {
            patientIdsQueryBuilder.should(QueryBuilders.matchQuery("patient.id", id));
        }

        BoolQueryBuilder careEventsQueryBuilder = QueryBuilders.boolQuery();
        for(String id : query.getCareEvents()) {
            careEventsQueryBuilder.should(QueryBuilders.matchQuery("careEvent.id", id));
        }

        BoolQueryBuilder phaseQueryBuilder = QueryBuilders.boolQuery();
        for(String phase : query.getPhases()) {
            phaseQueryBuilder.should(QueryBuilders.matchQuery("phase", phase));
        }

        BoolQueryBuilder statusQueryBuilder = QueryBuilders.boolQuery();
        for(String status : query.getStatuses()) {
            statusQueryBuilder.should(QueryBuilders.matchQuery("status", status));
        }

        BoolQueryBuilder lateralityQueryBuilder = QueryBuilders.boolQuery();
        for(String phase : query.getSides()) {
            lateralityQueryBuilder.should(QueryBuilders.matchQuery("careEvent.followupPlan.procedureBooking.side", phase));
        }

        BoolQueryBuilder typeQueryBuilder = QueryBuilders.boolQuery();
        for(String type : query.getTypes()) {
            typeQueryBuilder.should(QueryBuilders.matchQuery("type", type));
        }

        BoolQueryBuilder genderQueryBuilder = QueryBuilders.boolQuery();
        for(String gender : query.getGenders()) {
            genderQueryBuilder.should(QueryBuilders.matchQuery("careEvent.followupPlan.patient.gender", gender));
        }

        if(query.getMinAge() != null) {
            boolQueryBuilder.must(
                QueryBuilders.boolQuery().should(QueryBuilders.rangeQuery("careEvent.followupPlan.procedureBooking.patientAge").gte(query.getMinAge()))
                    .should(QueryBuilders.boolQuery().mustNot(QueryBuilders.existsQuery("careEvent.followupPlan.procedureBooking.patientAge")))
            );
        }

        if(query.getMaxAge() != null) {
            boolQueryBuilder.must(
                QueryBuilders.boolQuery().should(QueryBuilders.rangeQuery("careEvent.followupPlan.procedureBooking.patientAge").lte(query.getMaxAge()))
                    .should(QueryBuilders.boolQuery().mustNot(QueryBuilders.existsQuery("careEvent.followupPlan.procedureBooking.patientAge")))
            );
        }

        // we only add procedures clause if there are 1 or more procedure specified
        if(query.getProcedures().size() > 0){
            boolQueryBuilder.must(proceduresQueryBuilder);
        }

        // we only add locations clause if there are 1 or more locations specified
        if(query.getLocations().size() > 0){
            boolQueryBuilder.must(sourcesQueryBuilder);
        }

        // we only add phases clause if there are 1 or more phase specified
        if (query.getPhases().size() > 0){
            boolQueryBuilder.must(phaseQueryBuilder);
        }

        // we only add care events clause if there are 1 or more care events specified
        if (!query.getCareEvents().isEmpty()){
            boolQueryBuilder.must(careEventsQueryBuilder);
        }

        // we only add statuses clause if there are 1 or more statuses specified
        if (query.getStatuses().size() > 0){
            boolQueryBuilder.must(statusQueryBuilder);
        }

        // we only add laterality clause if there are 1 or more sides specified
        if (query.getSides().size() > 0){
            boolQueryBuilder.must(lateralityQueryBuilder);
        }

        // we only add consultants clause if there are 1 or more consultant specified
        if(query.getConsultants().size() > 0){
            boolQueryBuilder.must(consultantsQueryBuilder);
        }

        // we only add patient ids clause if there are 1 or more id specified
        if(query.getPatientIds().size() > 0){
            boolQueryBuilder.must(patientIdsQueryBuilder);
        }

        // we only add types clause if there are 1 or more type specified
        if(query.getTypes().size() > 0){
            boolQueryBuilder.must(typeQueryBuilder);
        }

        // we only add gender clause if there are 1 or more gender specified
        if (query.getGenders().size() > 0){
            boolQueryBuilder.must(genderQueryBuilder);
        }

        // we only add token clause if there is a token specified
        String token = query.getToken();
        log.info("token = {}", token);
        if("null".equalsIgnoreCase(token)) {
            log.debug("Resetting token");
            token = "";
        }
        if(token == null){
            log.debug("Token is null");
            log.debug("boolQueryBuilder = " + boolQueryBuilder);
        }
        if (token != null && token.length() > 2){
            BoolQueryBuilder tokenBuilder = QueryBuilders.boolQuery();
            // try to see if token is number, if it is try as nhs number otherwise try as other fields
            try {
                Long number = Long.parseLong(token);
                tokenBuilder.should(QueryBuilders.multiMatchQuery(number, "patient.nhsNumber", "patient.id").type(MultiMatchQueryBuilder.Type.PHRASE_PREFIX));
            } catch (NumberFormatException e) {
                tokenBuilder.should(QueryBuilders.multiMatchQuery(token, "patient.address.*").type(MultiMatchQueryBuilder.Type.PHRASE_PREFIX));
            }

            boolQueryBuilder.must(tokenBuilder);
        }

        log.debug("boolQueryBuilder = " + boolQueryBuilder);
        System.out.println(boolQueryBuilder);
        // build and return boolean query
        return getFacetedPageForQuery(boolQueryBuilder, pageable);
    }

    private FacetedPage<FollowupAction> getFacetedPageForQuery(QueryBuilder queryBuilder, Pageable pageable) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
            .withQuery(queryBuilder)
            .withSort(getSortParameters(pageable))
            .withPageable(pageable)
            .addAggregation(new TermsBuilder("types").field("type").size(5).order(Terms.Order.term(true)))
            .addAggregation(new TermsBuilder("procedures").field("careEvent.followupPlan.procedureBooking.primaryProcedure").size(100).order(Terms.Order.term(true)))
            .addAggregation(new TermsBuilder("consultants").field("careEvent.followupPlan.procedureBooking.consultantName").size(100).order(Terms.Order.term(true)))
            .addAggregation(new TermsBuilder("locations").field("careEvent.followupPlan.procedureBooking.hospitalSite").size(100).order(Terms.Order.term(true)))
            .addAggregation(new TermsBuilder("genders").field("careEvent.followupPlan.patient.gender").size(5).order(Terms.Order.term(true)))
            .addAggregation(new TermsBuilder("phases").field("phase").size(10).order(Terms.Order.term(true)))
            .build();

        FacetedPage<FollowupAction> page = elasticsearchTemplate.queryForPage(searchQuery, FollowupAction.class);

        return page;
    }


    private FieldSortBuilder getSortParameters(Pageable pageable) {
        List<Sort.Order> orders = new ArrayList<>() ;
        if (pageable != null) {
            for (Sort.Order order : pageable.getSort()) {
                String encapsulatedProperty = "("+order.getProperty() + ")";
                orders.add(new Sort.Order(order.getDirection(), encapsulatedProperty));
            }
        }

        SortOrder sortOrder = SortOrder.ASC;
        String sortField = "name";

        if (!orders.isEmpty()) {
            Sort.Order order = orders.get(0);
            if(order != null) {
                if(order.getProperty() != null) {
                    sortField = order.getProperty();
                }
                if(order.isDescending()) {
                    sortOrder = SortOrder.DESC;
                }
            }
        }

        return SortBuilders.fieldSort(sortField).order(sortOrder).unmappedType("string");
    }


    /**
     * Search for the followupAction corresponding to the query.
     *
     * @param query the query of the search
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    public Page<FollowupAction> searchQuestionnaire(QuestionnaireQueryModel query, Pageable pageable){
        log.debug("Request to search for a page of Questionnaire Response for query {}", query);

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        BoolQueryBuilder identifierQueryBuilder = QueryBuilders.boolQuery();
        for(String identifier : query.getIdentifier()) {
            identifierQueryBuilder.should(QueryBuilders.matchQuery("id", identifier));
        }

        BoolQueryBuilder parentQueryBuilder = QueryBuilders.boolQuery();
        for(String parent : query.getParent()) {
            parentQueryBuilder.should(QueryBuilders.matchPhraseQuery("careEvent.followupPlan.procedureBooking.id", parent));
        }

        BoolQueryBuilder questionnaireQueryBuilder = QueryBuilders.boolQuery();
        for(String questionnaire : query.getQuestionnaire()) {
            questionnaireQueryBuilder.should(QueryBuilders.matchPhraseQuery("questionnaire.id", questionnaire));
        }

        BoolQueryBuilder statusQueryBuilder = QueryBuilders.boolQuery();
        for(ActionStatus status : query.getStatus()) {
            statusQueryBuilder.should(QueryBuilders.matchQuery("status", status));
        }
        BoolQueryBuilder patientQueryBuilder = QueryBuilders.boolQuery();
        for(String patient : query.getPatient()) {
            patientQueryBuilder.should(QueryBuilders.matchQuery("patient.id", patient));
        }

        BoolQueryBuilder subjectQueryBuilder = QueryBuilders.boolQuery();
        for(String subject : query.getSubject()) {
            subjectQueryBuilder.should(QueryBuilders.matchQuery("patient.id", subject));
        }


        // we only add procedures clause if there are 1 or more procedure specified
        if(query.getIdentifier().size() > 0){
            boolQueryBuilder.must(identifierQueryBuilder);
        }

        // we only add locations clause if there are 1 or more locations specified
        if(query.getParent().size() > 0){
            boolQueryBuilder.must(parentQueryBuilder);
        }

        // we only add phases clause if there are 1 or more phase specified
        if (query.getQuestionnaire().size() > 0){
            boolQueryBuilder.must(questionnaireQueryBuilder);
        }

        // we only add care events clause if there are 1 or more care events specified
        if (query.getStatus().size() > 0){
            boolQueryBuilder.must(statusQueryBuilder);
        }

        // we only add statuses clause if there are 1 or more statuses specified
        if (query.getPatient().size() > 0){
            boolQueryBuilder.must(patientQueryBuilder);
        }

        // we only add laterality clause if there are 1 or more sides specified
        if (query.getSubject().size() > 0){
            boolQueryBuilder.must(subjectQueryBuilder);
        }

        log.debug("boolQueryBuilder = " + boolQueryBuilder);
        // build and return boolean query
        System.out.println(boolQueryBuilder);
        return getPageForQuery(boolQueryBuilder, pageable);
    }

    private Page<FollowupAction> getPageForQuery(QueryBuilder queryBuilder, Pageable pageable) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
            .withQuery(queryBuilder)
            .withSort(getSortParameters(pageable))
            .withPageable(pageable)
            .addAggregation(new TermsBuilder("types").field("type").size(5).order(Terms.Order.term(true)))
            .addAggregation(new TermsBuilder("procedures").field("careEvent.followupPlan.procedureBooking.primaryProcedure").size(100).order(Terms.Order.term(true)))
            .addAggregation(new TermsBuilder("consultants").field("careEvent.followupPlan.procedureBooking.consultantName").size(100).order(Terms.Order.term(true)))
            .addAggregation(new TermsBuilder("locations").field("careEvent.followupPlan.procedureBooking.hospitalSite").size(100).order(Terms.Order.term(true)))
            .addAggregation(new TermsBuilder("genders").field("careEvent.followupPlan.patient.gender").size(5).order(Terms.Order.term(true)))
            .addAggregation(new TermsBuilder("phases").field("phase").size(10).order(Terms.Order.term(true)))
            .build();

        Page<FollowupAction> page = elasticsearchTemplate.queryForPage(searchQuery, FollowupAction.class);

        return page;
    }
}
