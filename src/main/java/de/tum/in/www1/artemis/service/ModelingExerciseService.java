package de.tum.in.www1.artemis.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import de.tum.in.www1.artemis.domain.User;
import de.tum.in.www1.artemis.domain.modeling.ModelCluster;
import de.tum.in.www1.artemis.domain.modeling.ModelingExercise;
import de.tum.in.www1.artemis.repository.ModelClusterRepository;
import de.tum.in.www1.artemis.repository.ModelElementRepository;
import de.tum.in.www1.artemis.repository.ModelingExerciseRepository;
import de.tum.in.www1.artemis.service.messaging.InstanceMessageSendService;
import de.tum.in.www1.artemis.service.notifications.GroupNotificationScheduleService;
import de.tum.in.www1.artemis.web.rest.dto.PageableSearchDTO;
import de.tum.in.www1.artemis.web.rest.dto.SearchResultPageDTO;
import de.tum.in.www1.artemis.web.rest.errors.BadRequestAlertException;
import de.tum.in.www1.artemis.web.rest.util.PageUtil;

@Service
public class ModelingExerciseService {

    private final Logger log = LoggerFactory.getLogger(ModelingExerciseService.class);

    private final ModelingExerciseRepository modelingExerciseRepository;

    private final AuthorizationCheckService authCheckService;

    private final InstanceMessageSendService instanceMessageSendService;

    private final ModelClusterRepository modelClusterRepository;

    private final ModelElementRepository modelElementRepository;

    private final ModelAssessmentKnowledgeService modelAssessmentKnowledgeService;

    private final ModelingExerciseService modelingExerciseService;

    private final GroupNotificationScheduleService groupNotificationScheduleService;

    public ModelingExerciseService(ModelingExerciseRepository modelingExerciseRepository, AuthorizationCheckService authCheckService,
            InstanceMessageSendService instanceMessageSendService, ModelClusterRepository modelClusterRepository, ModelElementRepository modelElementRepository,
            ModelAssessmentKnowledgeService modelAssessmentKnowledgeService, ModelingExerciseService modelingExerciseService,
            GroupNotificationScheduleService groupNotificationScheduleService) {
        this.modelingExerciseRepository = modelingExerciseRepository;
        this.authCheckService = authCheckService;
        this.instanceMessageSendService = instanceMessageSendService;
        this.modelClusterRepository = modelClusterRepository;
        this.modelElementRepository = modelElementRepository;
        this.modelAssessmentKnowledgeService = modelAssessmentKnowledgeService;
        this.modelingExerciseService = modelingExerciseService;
        this.groupNotificationScheduleService = groupNotificationScheduleService;
    }

    /**
     * Creates a ModelingExercise
     *
     * @param modelingExercise the ModelingExercise to be created
     * @return the created ModelingExercise
     */
    public ModelingExercise createModelingExercise(ModelingExercise modelingExercise) {
        log.debug("Request to create ModelingExercise: {}", modelingExercise);

        if (modelingExercise.getId() != null) {
            throw new BadRequestAlertException("A new modeling exercise cannot already have an ID", ModelingExercise.ENTITY_NAME, "idExists");
        }
        if (modelingExercise.getTitle() == null) {
            throw new BadRequestAlertException("A new modeling exercise needs a title", ModelingExercise.ENTITY_NAME, "missingtitle");
        }
        // validates general settings: points, dates
        modelingExercise.validateGeneralSettings();
        // Valid exercises have set either a course or an exerciseGroup
        modelingExercise.checkCourseAndExerciseGroupExclusivity(ModelingExercise.ENTITY_NAME);

        // if exercise is created from scratch we create new knowledge instance
        modelingExercise.setKnowledge(modelAssessmentKnowledgeService.createNewKnowledge());
        ModelingExercise result = modelingExerciseRepository.save(modelingExercise);
        modelingExerciseService.scheduleOperations(result.getId());
        groupNotificationScheduleService.checkNotificationsForNewExercise(modelingExercise);

        return result;
    }

    /**
     * Search for all modeling exercises fitting a {@link PageableSearchDTO search query}. The result is paged,
     * meaning that there is only a predefined portion of the result returned to the user, so that the server doesn't
     * have to send hundreds/thousands of exercises if there are that many in Artemis.
     *
     * @param search         The search query defining the search term and the size of the returned page
     * @param isCourseFilter Whether to search in the courses for exercises
     * @param isExamFilter   Whether to search in the groups for exercises
     * @param user           The user for whom to fetch all available exercises
     * @return A wrapper object containing a list of all found exercises and the total number of pages
     */
    public SearchResultPageDTO<ModelingExercise> getAllOnPageWithSize(final PageableSearchDTO<String> search, final Boolean isCourseFilter, final Boolean isExamFilter,
            final User user) {
        final var pageable = PageUtil.createExercisePageRequest(search);
        final var searchTerm = search.getSearchTerm();
        Page<ModelingExercise> exercisePage = Page.empty();
        if (authCheckService.isAdmin(user)) {
            if (isCourseFilter && isExamFilter) {
                exercisePage = modelingExerciseRepository.queryBySearchTermInAllCoursesAndExams(searchTerm, pageable);
            }
            else if (isCourseFilter) {
                exercisePage = modelingExerciseRepository.queryBySearchTermInAllCourses(searchTerm, pageable);
            }
            else if (isExamFilter) {
                exercisePage = modelingExerciseRepository.queryBySearchTermInAllExams(searchTerm, pageable);
            }
        }
        else {
            if (isCourseFilter && isExamFilter) {
                exercisePage = modelingExerciseRepository.queryBySearchTermInAllCoursesAndExamsWhereEditorOrInstructor(searchTerm, user.getGroups(), pageable);
            }
            else if (isCourseFilter) {
                exercisePage = modelingExerciseRepository.queryBySearchTermInAllCoursesWhereEditorOrInstructor(searchTerm, user.getGroups(), pageable);
            }
            else if (isExamFilter) {
                exercisePage = modelingExerciseRepository.queryBySearchTermInAllExamsWhereEditorOrInstructor(searchTerm, user.getGroups(), pageable);
            }
        }
        return new SearchResultPageDTO<>(exercisePage.getContent(), exercisePage.getTotalPages());
    }

    public void scheduleOperations(Long modelingExerciseId) {
        instanceMessageSendService.sendModelingExerciseSchedule(modelingExerciseId);
    }

    public void cancelScheduledOperations(Long modelingExerciseId) {
        instanceMessageSendService.sendModelingExerciseScheduleCancel(modelingExerciseId);
    }

    /**
     * Delete clusters and elements of a modeling exercise
     *
     * @param modelingExercise modeling exercise clusters and elements belong to
     */
    public void deleteClustersAndElements(ModelingExercise modelingExercise) {
        List<ModelCluster> clustersToDelete = modelClusterRepository.findAllByExerciseIdWithEagerElements(modelingExercise.getId());

        for (ModelCluster cluster : clustersToDelete) {
            modelElementRepository.deleteAll(cluster.getModelElements());
            modelClusterRepository.deleteById(cluster.getId());
        }

    }

}
