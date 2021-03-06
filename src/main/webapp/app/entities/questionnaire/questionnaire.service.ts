import { Injectable } from '@angular/core';
import { Http, Response } from '@angular/http';
import { Observable } from 'rxjs/Rx';
import { SERVER_API_URL } from '../../app.constants';

import { Questionnaire } from './questionnaire.model';
import { ResponseWrapper, createRequestOption } from '../../shared';

@Injectable()
export class QuestionnaireService {

    private resourceUrl = SERVER_API_URL + 'api/questionnaires';
    private procedureResourceUrl = SERVER_API_URL + 'api/procedures';
    private resourceSearchUrl = SERVER_API_URL + 'api/_search/questionnaires';

    constructor(private http: Http) { }

    create(questionnaire: Questionnaire): Observable<Questionnaire> {
        const copy = this.convert(questionnaire);
        return this.http.post(this.resourceUrl, copy).map((res: Response) => {
            const jsonResponse = res.json();
            return this.convertItemFromServer(jsonResponse);
        });
    }

    update(questionnaire: Questionnaire): Observable<Questionnaire> {
        const copy = this.convert(questionnaire);
        return this.http.put(this.resourceUrl, copy).map((res: Response) => {
            const jsonResponse = res.json();
            return this.convertItemFromServer(jsonResponse);
        });
    }

    find(id: number): Observable<Questionnaire> {
        return this.http.get(`${this.resourceUrl}/${id}`).map((res: Response) => {
            const jsonResponse = res.json();
            return this.convertItemFromServer(jsonResponse);
        });
    }

    query(req?: any): Observable<ResponseWrapper> {
        const options = createRequestOption(req);
        return this.http.get(this.resourceUrl, options)
            .map((res: Response) => this.convertResponse(res));
    }

    questinnairesForProcedureLocalCode(id: any): Observable<ResponseWrapper> {
        return this.http.get(this.procedureResourceUrl + `/${id}/questionnaires`)
            .map((res: Response) => this.convertToSelectOption(res));
    }

    allAsSelectOptions(req?: any): Observable<ResponseWrapper> {
        const options = createRequestOption(req);
        return this.http.get(this.resourceUrl, options)
            .map((res: Response) => this.convertToSelectOption(res));
    }

    delete(id: number): Observable<Response> {
        return this.http.delete(`${this.resourceUrl}/${id}`);
    }

    search(req?: any): Observable<ResponseWrapper> {
        const options = createRequestOption(req);
        return this.http.get(this.resourceSearchUrl, options)
            .map((res: any) => this.convertResponse(res));
    }

    private convertResponse(res: Response): ResponseWrapper {
        const jsonResponse = res.json();
        const result = [];
        for (let i = 0; i < jsonResponse.length; i++) {
            result.push(this.convertItemFromServer(jsonResponse[i]));
        }
        return new ResponseWrapper(res.headers, result, res.status);
    }

    /**
     * Convert a returned JSON object to Questionnaire.
     */
    private convertItemFromServer(json: any): Questionnaire {
        const entity: Questionnaire = Object.assign(new Questionnaire(), json);
        return entity;
    }

    /**
     * Convert a Questionnaire to a JSON which can be sent to the server.
     */
    private convert(questionnaire: Questionnaire): Questionnaire {
        const copy: Questionnaire = Object.assign({}, questionnaire);
        return copy;
    }

    /**
     * Convert the result into an array of {value, label} items for use in UI select
     * @param res
     * @returns {any}
     */
    private convertToSelectOption(res: Response): ResponseWrapper {
        const jsonResponse = res.json();
        const result = [];
        for (let i = 0; i < jsonResponse.length; i++) {
            result.push({value: jsonResponse[i].id, label: jsonResponse[i].name});
        }
        return new ResponseWrapper(res.headers, result, res.status);
    }
}
