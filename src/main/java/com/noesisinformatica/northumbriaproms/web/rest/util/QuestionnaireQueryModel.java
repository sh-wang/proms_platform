package com.noesisinformatica.northumbriaproms.web.rest.util;

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

import com.noesisinformatica.northumbriaproms.domain.enumeration.ActionStatus;
import org.hl7.fhir.dstu3.model.QuestionnaireResponse;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class QuestionnaireQueryModel {
    List<String> identifier = new ArrayList<>();
    List<String> parent = new ArrayList<>();
    List<String> questionnaire = new ArrayList<>();
    List<ActionStatus> status = new ArrayList<>();
    List<Date> authored = new ArrayList<>();
    List<String> patient = new ArrayList<>();
    List<String> subject = new ArrayList<>();
    List<String> author = new ArrayList<>();


    public List<String> getAuthor() {
        return author;
    }

    public void setAuthor(List<String> author) {
        this.author = author;
    }

    public List<String> getIdentifier() {
        return identifier;
    }

    public void setIdentifier(List<String> identifier) {
        this.identifier = identifier;
    }

    public List<String> getParent() {
        return parent;
    }

    public void setParent(List<String> parent) {
        this.parent = parent;
    }

    public List<String> getQuestionnaire() {
        return questionnaire;
    }

    public void setQuestionnaire(List<String> questionnaire) {
        this.questionnaire = questionnaire;
    }

    public List<ActionStatus> getStatus() {
        return status;
    }

    public void setStatus(List<ActionStatus> status) {
        this.status = status;
    }

    public List<String> getPatient() {
        return patient;
    }

    public void setPatient(List<String> patient) {
        this.patient = patient;
    }

    public List<String> getSubject() {
        return subject;
    }

    public void setSubject(List<String> subject) {
        this.subject = subject;
    }

    public List<Date> getAuthored() {
        return authored;
    }

    public void setAuthored(List<Date> authored) {
        this.authored = authored;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("QuestionnaireQueryModel{");
        sb.append("identifier=").append(identifier);
        sb.append(", parent=").append(parent);
        sb.append(", questionnaire=").append(questionnaire);
        sb.append(", status=").append(status);
        sb.append(", subject=").append(subject);
        sb.append(", authored=").append(authored);
        sb.append(", patient='").append(patient).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
