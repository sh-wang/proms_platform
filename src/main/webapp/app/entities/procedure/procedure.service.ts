import { Injectable } from '@angular/core';
import { Http, Response } from '@angular/http';
import { Observable } from 'rxjs/Rx';
import { SERVER_API_URL } from '../../app.constants';

import { Procedure } from './procedure.model';
import { ResponseWrapper, createRequestOption } from '../../shared';

@Injectable()
export class ProcedureService {

    private resourceUrl = SERVER_API_URL + 'api/procedures';
    private resourceSearchUrl = SERVER_API_URL + 'api/_search/procedures';

    constructor(private http: Http) { }

    create(procedure: Procedure): Observable<Procedure> {
        const copy = this.convert(procedure);
        return this.http.post(this.resourceUrl, copy).map((res: Response) => {
            const jsonResponse = res.json();
            return this.convertItemFromServer(jsonResponse);
        });
    }

    update(procedure: Procedure): Observable<Procedure> {
        const copy = this.convert(procedure);
        return this.http.put(this.resourceUrl, copy).map((res: Response) => {
            const jsonResponse = res.json();
            return this.convertItemFromServer(jsonResponse);
        });
    }

    find(id: number): Observable<Procedure> {
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
     * Convert a returned JSON object to Procedure.
     */
    private convertItemFromServer(json: any): Procedure {
        const entity: Procedure = Object.assign(new Procedure(), json);
        return entity;
    }

    /**
     * Convert returned result to Array of 'value, label' objects for use as UI select options
     * @param res
     * @returns {any}
     */
    private convertToSelectOption(res: Response): ResponseWrapper {
        const jsonResponse = res.json();
        const result = [];
        for (let i = 0; i < jsonResponse.length; i++) {
            result.push({value: jsonResponse[i].localCode, label: jsonResponse[i].name});
        }
        return new ResponseWrapper(res.headers, result, res.status);
    }

    /**
     * Convert a Procedure to a JSON which can be sent to the server.
     */
    private convert(procedure: Procedure): Procedure {
        const copy: Procedure = Object.assign({}, procedure);
        return copy;
    }
}
