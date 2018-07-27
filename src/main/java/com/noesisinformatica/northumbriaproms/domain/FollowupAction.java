package com.noesisinformatica.northumbriaproms.domain;

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

import com.noesisinformatica.northumbriaproms.domain.enumeration.ActionPhase;
import com.noesisinformatica.northumbriaproms.domain.enumeration.ActionStatus;
import com.noesisinformatica.northumbriaproms.domain.enumeration.ActionType;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


/**
 * A FollowupAction.
 */
@Entity
@Table(name = "followup_action")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "followupaction")
public class FollowupAction extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "phase")
    private ActionPhase phase;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ActionStatus status;

    @Column(name = "scheduled_date")
    private LocalDate scheduledDate;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "jhi_type")
    private ActionType type;

    @Column(name = "outcome_score")
    private Integer outcomeScore;

    @Column(name = "outcome_comment")
    private String outcomeComment;

    @Column(name = "completed_date")
    private LocalDate completedDate;

    @ManyToOne(optional = false)
    @NotNull
    private CareEvent careEvent;

    @ManyToOne(optional = false)
    @NotNull
    private Patient patient;

    @ManyToOne
    private Questionnaire questionnaire;

    @OneToMany(mappedBy = "followupAction", cascade = {CascadeType.ALL}, orphanRemoval = true, fetch = FetchType.EAGER)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<ResponseItem> responseItems = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ActionPhase getPhase() {
        return phase;
    }

    public FollowupAction phase(ActionPhase phase) {
        this.phase = phase;
        return this;
    }

    public void setPhase(ActionPhase phase) {
        this.phase = phase;
    }

    public LocalDate getScheduledDate() {
        return scheduledDate;
    }

    public FollowupAction scheduledDate(LocalDate scheduledDate) {
        this.scheduledDate = scheduledDate;
        return this;
    }

    public void setScheduledDate(LocalDate scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public String getName() {
        return name;
    }

    public FollowupAction name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ActionType getType() {
        return type;
    }

    public FollowupAction type(ActionType type) {
        this.type = type;
        return this;
    }

    public void setType(ActionType type) {
        this.type = type;
    }

    public Integer getOutcomeScore() {
        return outcomeScore;
    }

    public FollowupAction outcomeScore(Integer outcomeScore) {
        this.outcomeScore = outcomeScore;
        return this;
    }

    public void setOutcomeScore(Integer outcomeScore) {
        this.outcomeScore = outcomeScore;
    }

    public String getOutcomeComment() {
        return outcomeComment;
    }

    public FollowupAction outcomeComment(String outcomeComment) {
        this.outcomeComment = outcomeComment;
        return this;
    }

    public void setOutcomeComment(String outcomeComment) {
        this.outcomeComment = outcomeComment;
    }

    public LocalDate getCompletedDate() {
        return completedDate;
    }

    public FollowupAction completedDate(LocalDate completedDate) {
        this.completedDate = completedDate;
        return this;
    }

    public void setCompletedDate(LocalDate completedDate) {
        this.completedDate = completedDate;
    }

    public CareEvent getCareEvent() {
        return careEvent;
    }

    public FollowupAction careEvent(CareEvent careEvent) {
        this.careEvent = careEvent;
        return this;
    }

    public void setCareEvent(CareEvent careEvent) {
        this.careEvent = careEvent;
    }

    public Patient getPatient() {
        return patient;
    }

    public FollowupAction patient(Patient patient) {
        this.patient = patient;
        return this;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Questionnaire getQuestionnaire() {
        return questionnaire;
    }

    public FollowupAction questionnaire(Questionnaire questionnaire) {
        this.questionnaire = questionnaire;
        return this;
    }

    public void setQuestionnaire(Questionnaire questionnaire) {
        this.questionnaire = questionnaire;
    }

    public Set<ResponseItem> getResponseItems() {
        return responseItems;
    }

    public void setResponseItems(Set<ResponseItem> responseItems) {
        // associate all items with this followup action
        responseItems.forEach(responseItem -> {
            responseItem.setFollowupAction(this);
        });
        this.responseItems = responseItems;
    }

    public ActionStatus getStatus() {
        return status;
    }

    public void setStatus(ActionStatus status) {
        this.status = status;
    }

    public FollowupAction status(ActionStatus status) {
        this.status = status;
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FollowupAction followupAction = (FollowupAction) o;
        if (followupAction.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), followupAction.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "FollowupAction{" +
            "id=" + getId() +
            ", phase='" + getPhase() + "'" +
            ", status='" + getStatus() + "'" +
            ", scheduledDate='" + getScheduledDate() + "'" +
            ", name='" + getName() + "'" +
            ", type='" + getType() + "'" +
            ", responseItems=" + getResponseItems() +
            ", outcomeScore=" + getOutcomeScore() +
            ", outcomeComment='" + getOutcomeComment() + "'" +
            ", completedDate='" + getCompletedDate() + "'" +
            "}";
    }

}
