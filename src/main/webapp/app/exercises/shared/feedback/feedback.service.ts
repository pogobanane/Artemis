import { Injectable } from '@angular/core';
import { Feedback } from 'app/entities/feedback.model';
import { Result } from 'app/entities/result.model';
import { ResultService } from 'app/exercises/shared/result/result.service';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Participation } from 'app/entities/participation/participation.model';
import { HttpClient } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class FeedbackService {
    private feedbackResourceUrl = SERVER_API_URL + 'api/feedbacks';

    constructor(private http: HttpClient, private resultService: ResultService) {}

    /**
     * Filters the feedback based on the filter input
     * Used e.g. when we want to show certain test cases viewed from the exercise description
     * @param feedbacks The full list of feedback
     * @param filter an array of strings that the feedback needs to contain in its text attribute.
     */
    public filterFeedback = (feedbacks: Feedback[], filter: string[]): Feedback[] => {
        if (!filter) {
            return [...feedbacks];
        }
        return feedbacks.filter(({ text }) => text && filter.includes(text));
    };

    /**
     * Loads the missing feedback details
     * @param participationId the current participation
     * @param result
     */
    public getFeedbacksForResult(participationId: number, result: Result): Observable<Feedback[]> {
        return this.resultService.getFeedbackDetailsForResult(participationId, result).pipe(map(({ body: feedbackList }) => feedbackList!));
    }

    /**
     * Loads the missing feedback details
     * @param participation the current participation
     * @param result
     */
    public getFeedbackDTOForResult(participation: Participation, result: Result): Observable<Feedback[]> {
        return this.http
            .get<Feedback[]>(`${this.feedbackResourceUrl}/${participation.id}/results/${result.id}/feedback`, { observe: 'response' })
            .pipe(map((res) => res.body ?? []));
    }
}
