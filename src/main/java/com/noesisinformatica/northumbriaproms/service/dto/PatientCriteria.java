package com.noesisinformatica.northumbriaproms.service.dto;

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

import com.noesisinformatica.northumbriaproms.domain.enumeration.GenderType;
import io.github.jhipster.service.filter.Filter;
import io.github.jhipster.service.filter.LocalDateFilter;
import io.github.jhipster.service.filter.LongFilter;
import io.github.jhipster.service.filter.StringFilter;

import java.io.Serializable;



/**
 * Criteria class for the Patient entity. This class is used in PatientResource to
 * receive all the possible filtering options from the Http GET request parameters.
 * For example the following could be a valid requests:
 * <code> /patients?id.greaterThan=5&amp;attr1.contains=something&amp;attr2.specified=false</code>
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class PatientCriteria implements Serializable {
    /**
     * Class for filtering GenderType
     */
    public static class GenderTypeFilter extends Filter<GenderType> {
    }

    private static final long serialVersionUID = 1L;


    private LongFilter id;

    private StringFilter familyName;

    private StringFilter givenName;

    private LocalDateFilter birthDate;

    private GenderTypeFilter gender;

    private LongFilter nhsNumber;

    private StringFilter email;

    private LongFilter addressId;

    private LongFilter procedureBookingsId;

    public PatientCriteria() {
    }

    public LongFilter getId() {
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public StringFilter getFamilyName() {
        return familyName;
    }

    public void setFamilyName(StringFilter familyName) {
        this.familyName = familyName;
    }

    public StringFilter getGivenName() {
        return givenName;
    }

    public void setGivenName(StringFilter givenName) {
        this.givenName = givenName;
    }

    public LocalDateFilter getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDateFilter birthDate) {
        this.birthDate = birthDate;
    }

    public GenderTypeFilter getGender() {
        return gender;
    }

    public void setGender(GenderTypeFilter gender) {
        this.gender = gender;
    }

    public LongFilter getNhsNumber() {
        return nhsNumber;
    }

    public void setNhsNumber(LongFilter nhsNumber) {
        this.nhsNumber = nhsNumber;
    }

    public StringFilter getEmail() {
        return email;
    }

    public void setEmail(StringFilter email) {
        this.email = email;
    }

    public LongFilter getAddressId() {
        return addressId;
    }

    public void setAddressId(LongFilter addressId) {
        this.addressId = addressId;
    }

    public LongFilter getProcedureBookingsId() {
        return procedureBookingsId;
    }

    public void setProcedureBookingsId(LongFilter procedureBookingsId) {
        this.procedureBookingsId = procedureBookingsId;
    }

    @Override
    public String toString() {
        return "PatientCriteria{" +
                (id != null ? "id=" + id + ", " : "") +
                (familyName != null ? "familyName=" + familyName + ", " : "") +
                (givenName != null ? "givenName=" + givenName + ", " : "") +
                (birthDate != null ? "birthDate=" + birthDate + ", " : "") +
                (gender != null ? "gender=" + gender + ", " : "") +
                (nhsNumber != null ? "nhsNumber=" + nhsNumber + ", " : "") +
                (email != null ? "email=" + email + ", " : "") +
                (addressId != null ? "addressId=" + addressId + ", " : "") +
                (procedureBookingsId != null ? "procedureBookingsId=" + procedureBookingsId + ", " : "") +
            "}";
    }

}
