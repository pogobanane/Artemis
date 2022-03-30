import { AfterViewInit, Component, ElementRef, Input, OnDestroy, OnInit, QueryList, ViewChild, ViewChildren } from '@angular/core';
import { ActivatedRoute, Params } from '@angular/router';
import { CourseWideContext, PageType, PostContextFilter, PostSortCriterion, SortDirection } from 'app/shared/metis/metis.util';
import { combineLatest, Subscription } from 'rxjs';
import { Course } from 'app/entities/course.model';
import { Exercise } from 'app/entities/exercise.model';
import { Lecture } from 'app/entities/lecture.model';
import { MetisService } from 'app/shared/metis/metis.service';
import { Post } from 'app/entities/metis/post.model';
import { FormBuilder, FormGroup } from '@angular/forms';
import { ButtonType } from 'app/shared/components/button.component';
import { HttpResponse } from '@angular/common/http';
import { CourseManagementService } from 'app/course/manage/course-management.service';
import { faFilter, faLongArrowAltDown, faLongArrowAltUp, faPlus, faSearch, faTimes } from '@fortawesome/free-solid-svg-icons';
import { ChatSession } from 'app/entities/metis/chat.session/chat-session.model';
import { ITEMS_PER_PAGE } from 'app/shared/constants/pagination.constants';

@Component({
    selector: 'jhi-post-overview',
    templateUrl: './post-overview.component.html',
    styleUrls: ['./post-overview.component.scss'],
    providers: [MetisService],
})
export class PostOverviewComponent implements OnInit, OnDestroy, AfterViewInit {
    entitiesPerPageTranslation = 'organizationManagement.userSearch.usersPerPage';
    showAllEntitiesTranslation = 'organizationManagement.userSearch.showAllUsers';

    @ViewChildren('postingThread') messages: QueryList<any>;
    @ViewChild('container') content: ElementRef;

    @Input() courseMessagesPageFlag: boolean;
    private previousScrollDistanceFromTop: number;
    // as set in the css class '.posting-infinite-scroll-container'
    private messagesContainerHeight = 350;

    /**
     * receives and updates the selected chatSession, fetching its posts
     * @param activeChatSession selectedChatSessiob
     */
    @Input() set activeChatSession(activeChatSession: ChatSession) {
        if (this.courseMessagesPageFlag && activeChatSession) {
            this.chatSession = activeChatSession;
            if (this.course) {
                this.onSelectContext();
            }
        }
    }

    readonly PageType = PageType;
    readonly ButtonType = ButtonType;

    course?: Course;
    exercises?: Exercise[];
    lectures?: Lecture[];
    currentPostContextFilter: PostContextFilter;
    currentSortCriterion = PostSortCriterion.CREATION_DATE;
    currentSortDirection = SortDirection.DESCENDING;
    searchText?: string;
    chatSession?: ChatSession;
    formGroup: FormGroup;
    createdPost: Post;
    posts: Post[];
    isLoading = true;
    inlineInputEnabled = true;
    totalItems = 0;
    pagingEnabled = true;
    itemsPerPage = ITEMS_PER_PAGE;
    page = 1;
    readonly CourseWideContext = CourseWideContext;
    readonly SortBy = PostSortCriterion;
    readonly SortDirection = SortDirection;
    readonly pageType = PageType.OVERVIEW;

    private postsSubscription: Subscription;
    private paramSubscription: Subscription;
    private totalItemsSubscription: Subscription;
    private scrollBottomSubscription: Subscription;

    // Icons
    faPlus = faPlus;
    faTimes = faTimes;
    faFilter = faFilter;
    faSearch = faSearch;
    faLongArrowAltUp = faLongArrowAltUp;
    faLongArrowAltDown = faLongArrowAltDown;

    constructor(
        protected metisService: MetisService,
        private activatedRoute: ActivatedRoute,
        private courseManagementService: CourseManagementService,
        private formBuilder: FormBuilder,
    ) {}

    /**
     * on initialization: initializes the metis service, fetches the posts for the course, resets all user inputs and selects the defaults,
     * creates the subscription to posts to stay updated on any changes of posts in this course
     */
    ngOnInit(): void {
        this.paramSubscription = combineLatest({
            params: this.activatedRoute.parent!.parent!.params,
            queryParams: this.activatedRoute.parent!.parent!.queryParams,
        }).subscribe((routeParams: { params: Params; queryParams: Params }) => {
            const { params, queryParams } = routeParams;
            const courseId = params.courseId;
            this.searchText = queryParams.searchText;
            this.page = 1;
            this.courseManagementService.findOneForDashboard(courseId).subscribe((res: HttpResponse<Course>) => {
                if (res.body !== undefined) {
                    this.course = res.body!;
                    if (this.course?.lectures) {
                        this.lectures = this.course.lectures.sort(this.overviewContextSortFn);
                    }
                    if (this.course?.exercises) {
                        this.exercises = this.course.exercises.sort(this.overviewContextSortFn);
                    }
                    this.metisService.setCourse(this.course!);
                    this.metisService.setPageType(this.pageType);
                    if (!(this.courseMessagesPageFlag && !this.chatSession)) {
                        this.metisService.getFilteredPosts({
                            courseId: this.course!.id,
                            chatSessionId: this.chatSession?.id,
                            searchText: this.searchText ? this.searchText : undefined,
                            postSortCriterion: this.currentSortCriterion,
                            sortingOrder: this.currentSortDirection,
                            pagingEnabled: this.pagingEnabled,
                            page: this.page - 1,
                            pageSize: this.itemsPerPage,
                        });
                    } else {
                        // if user has no chatSessions to load on messages page
                        this.isLoading = false;
                    }
                    this.resetCurrentFilter();
                    this.createEmptyPost();
                    this.resetFormGroup();
                }
            });
        });
        this.postsSubscription = this.metisService.posts.pipe().subscribe((posts: Post[]) => {
            if (!this.courseMessagesPageFlag) {
                this.posts = posts;
            } else {
                this.posts = posts.slice().reverse();
                this.previousScrollDistanceFromTop = this.content.nativeElement.scrollHeight - this.content.nativeElement.scrollTop;
            }
            this.isLoading = false;
        });
        this.totalItemsSubscription = this.metisService.totalItems.pipe().subscribe((totalItems: number) => {
            this.totalItems = totalItems;
        });
    }

    /**
     * subscribes to changes in the message container, in order display the bottom of the list at the messages page
     */
    ngAfterViewInit() {
        if (this.courseMessagesPageFlag) {
            this.scrollBottomSubscription = this.messages.changes.subscribe(this.scrollToBottom);
        }
    }

    /**
     * by default, the form group fields are set to show all posts in a course by descending creation date
     */
    resetFormGroup(): void {
        this.formGroup = this.formBuilder.group({
            context: [this.currentPostContextFilter],
            sortBy: [PostSortCriterion.CREATION_DATE],
            filterToUnresolved: false,
            filterToOwn: false,
            filterToAnsweredOrReacted: false,
        });
    }

    ngOnDestroy(): void {
        this.paramSubscription?.unsubscribe();
        this.postsSubscription?.unsubscribe();
        this.totalItemsSubscription?.unsubscribe();
        this.scrollBottomSubscription?.unsubscribe();
    }

    /**
     *  metis service is invoked to deliver another page of posts, filtered and sorted on the backend
     */
    onSelectPage(): void {
        this.setFilterAndSort();
        this.metisService.getFilteredPosts(this.currentPostContextFilter, false);
    }

    /**
     * on changing any filter, the metis service is invoked to deliver the first page of posts for the
     * currently set context, filtered and sorted on the backend
     */
    onSelectContext(): void {
        this.page = 1;
        this.setFilterAndSort();
        this.metisService.getFilteredPosts(this.currentPostContextFilter);
    }

    /**
     * on changing the sort direction via icon, the metis service is invoked to deliver the posts for the currently set context,
     * sorted on the backend
     */
    onChangeSortDir(): void {
        // flip sort direction
        this.currentSortDirection = this.currentSortDirection === SortDirection.DESCENDING ? SortDirection.ASCENDING : SortDirection.DESCENDING;
        this.onSelectContext();
    }

    /**
     * scrolls to the bottom of the message container if in messages page and posts exist for the selected chatSession and
     * only the first page is fetched or user is scrolled to the last post of the chatSession
     */
    scrollToBottom = () => {
        try {
            if (
                this.courseMessagesPageFlag &&
                this.posts.length > 0 &&
                ((this.content.nativeElement.scrollTop === 0 && this.page === 1) || this.previousScrollDistanceFromTop === this.messagesContainerHeight)
            ) {
                this.content.nativeElement.scrollTop = this.content.nativeElement.scrollHeight;
            }
        } catch (err) {}
    };

    /**
     * required for distinguishing different select options for the context selector,
     * Angular needs to be able to identify the currently selected option
     */
    compareContextFilterOptionFn(option1: PostContextFilter, option2: PostContextFilter) {
        if (option1.exerciseId && option2.exerciseId) {
            return option1.exerciseId === option2.exerciseId;
        } else if (option1.lectureId && option2.lectureId) {
            return option1.lectureId === option2.lectureId;
        } else if (option1.courseWideContext && option2.courseWideContext) {
            return option1.courseWideContext === option2.courseWideContext;
        } else if (option1.courseId && option2.courseId) {
            return option1.courseId === option2.courseId;
        }
        return false;
    }

    /**
     * required for distinguishing different select options for the sort selector (sortBy, and sortDirection),
     * Angular needs to be able to identify the currently selected option
     */
    comparePostSortOptionFn(option1: PostSortCriterion | SortDirection, option2: PostSortCriterion | SortDirection) {
        return option1 === option2;
    }

    /**
     * sort context (lecture, exercise) by title
     **/
    private overviewContextSortFn = (contextA: Lecture | Exercise, contextB: Lecture | Exercise): number => {
        const titleA = contextA.title!.toUpperCase(); // ignore capitalization
        const titleB = contextB.title!.toUpperCase(); // ignore capitalization
        if (titleA < titleB) {
            return -1;
        }
        if (titleA > titleB) {
            return 1;
        }
        return 0;
    };

    /**
     * invoke metis service to create an empty default post that is needed on initialization of a modal to create a post,
     * this empty post has a default course-wide context as well as the course set as context
     **/
    createEmptyPost(): void {
        this.createdPost = this.metisService.createEmptyPostForContext(
            this.currentPostContextFilter.courseWideContext,
            this.exercises?.find((exercise) => exercise.id === this.currentPostContextFilter.exerciseId),
            this.lectures?.find((lecture) => lecture.id === this.currentPostContextFilter.lectureId),
            this.chatSession,
        );
    }

    /**
     * defines a function that returns the post id as unique identifier,
     * by this means, Angular determines which post in the collection of posts has to be reloaded/destroyed on changes
     */
    postsTrackByFn = (index: number, post: Post): number => post.id!;

    /**
     * sets the filter and sort options after receiving user input
     */
    private setFilterAndSort(): void {
        this.currentPostContextFilter = {
            courseId: undefined,
            courseWideContext: undefined,
            exerciseId: undefined,
            lectureId: undefined,
            ...this.formGroup?.get('context')?.value,
            searchText: this.searchText ? this.searchText : undefined,
            chatSessionId: this.chatSession?.id,
            pagingEnabled: this.pagingEnabled,
            page: this.page - 1,
            pageSize: this.itemsPerPage,
            filterToUnresolved: this.formGroup.get('filterToUnresolved')?.value,
            filterToOwn: this.formGroup.get('filterToOwn')?.value,
            filterToAnsweredOrReacted: this.formGroup.get('filterToAnsweredOrReacted')?.value,
            postSortCriterion: this.formGroup.get('sortBy')?.value,
            sortingOrder: this.currentSortDirection,
        };
    }

    /**
     * sets the current filter for context (default: course) and content (default: undefined)
     */
    private resetCurrentFilter(): void {
        this.currentPostContextFilter = {
            courseId: this.course!.id,
            chatSessionId: this.chatSession?.id,
            courseWideContext: undefined,
            exerciseId: undefined,
            lectureId: undefined,
            searchText: undefined,
            filterToUnresolved: false,
            filterToOwn: false,
            filterToAnsweredOrReacted: false,
            postSortCriterion: PostSortCriterion.CREATION_DATE,
            sortingOrder: SortDirection.DESCENDING,
        };
    }

    /**
     * fetches next page of posts on user scroll event
     */
    fetchNextPage() {
        if (this.posts.length < this.totalItems) {
            this.page += 1;
            this.onSelectPage();
        }
    }
}
