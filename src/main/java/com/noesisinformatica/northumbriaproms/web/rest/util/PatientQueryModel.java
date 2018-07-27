package com.noesisinformatica.northumbriaproms.web.rest.util;

import com.noesisinformatica.northumbriaproms.domain.enumeration.ActionStatus;
import com.noesisinformatica.northumbriaproms.domain.enumeration.GenderType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class PatientQueryModel {
    List<String> address = new ArrayList<>();
    List<Date> birthDate = new ArrayList<>();
    List<String> familyName = new ArrayList<>();
    List<String> givenName = new ArrayList<>();
    List<String> email = new ArrayList<>();
    List<Long> nhsNumber = new ArrayList<>();
    List<GenderType> gender = new ArrayList<>();
    List<String> name = new ArrayList<>();

    public List<String> getAddress() {
        return address;
    }

    public void setAddress(List<String> address) {
        this.address = address;
    }

    public List<Date> getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(List<Date> birthDate) {
        this.birthDate = birthDate;
    }

    public List<String> getFamilyName() {
        return familyName;
    }

    public void setFamilyName(List<String> familyName) {
        this.familyName = familyName;
    }

    public List<String> getGivenName() {
        return givenName;
    }

    public void setGivenName(List<String> givenName) {
        this.givenName = givenName;
    }

    public List<String> getEmail() {
        return email;
    }

    public void setEmail(List<String> email) {
        this.email = email;
    }

    public List<Long> getNhsNumber() {
        return nhsNumber;
    }

    public void setNhsNumber(List<Long> nhsNumber) {
        this.nhsNumber = nhsNumber;
    }

    public List<GenderType> getGender() {
        return gender;
    }

    public void setGender(List<GenderType> gender) {
        this.gender = gender;
    }

    public List<String> getName() {
        return name;
    }

    public void setName(List<String> name) {
        this.name = name;
    }


    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("QuestionnaireQueryModel{");
        sb.append("address_postalcode").append(address);
        sb.append(", birthdate=").append(birthDate);
        sb.append(", family=").append(familyName);
        sb.append(", given=").append(givenName);
        sb.append(", email=").append(email);
        sb.append(", identifier=").append(nhsNumber);
        sb.append(", gender=").append(gender);
        sb.append(", name=").append(name).append('\'');
        sb.append('}');
        return sb.toString();
    }
}

