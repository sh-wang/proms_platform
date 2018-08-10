package com.noesisinformatica.northumbriaproms.web.rest.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.noesisinformatica.northumbriaproms.domain.enumeration.ActionStatus;

public class QuestionnaireQueryModel {
    List<String> identifier = new ArrayList<>();
    List<String> parent = new ArrayList<>();
    List<String> questionnaire = new ArrayList<>();
    List<ActionStatus> status = new ArrayList<>();
    List<Date> authored = new ArrayList<>();
    List<String> patient = new ArrayList<>();
    List<String> familyName = new ArrayList<>();
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

    public List<String> getFamilyName() {
        return familyName;
    }

    public void setFamilyName(List<String> familyName) {
        this.familyName = familyName;
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
        sb.append(", familyName=").append(familyName);
        sb.append(", authored=").append(authored);
        sb.append(", patient='").append(patient).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
