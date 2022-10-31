package uk.gov.hmcts.reform.wataskconfigurationtemplate.dmn;

import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableImpl;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.wataskconfigurationtemplate.DmnDecisionTableBaseUnitTest;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.reform.wataskconfigurationtemplate.DmnDecisionTable.WA_TASK_INITIATION_WA_WACASETYPE;

class CamundaTaskWaInitiationTest extends DmnDecisionTableBaseUnitTest {

    @BeforeAll
    public static void initialization() {
        CURRENT_DMN_DECISION_TABLE = WA_TASK_INITIATION_WA_WACASETYPE;
    }

    @ParameterizedTest
    @MethodSource("scenarioProvider")
    void given_input_should_return_outcome_dmn(String eventId,
                                               String postEventState,
                                               String appealType,
                                               List<Map<String, Object>> expectedDmnOutcome) {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("eventId", eventId);
        inputVariables.putValue("postEventState", postEventState);
        inputVariables.putValue("appealType", appealType);

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);

        assertThat(dmnDecisionTableResult.getResultList(), is(expectedDmnOutcome));
    }

    public static Stream<Arguments> scenarioProvider() {

        return Stream.of(
            Arguments.of(
                "CREATE", "TODO", "anything",
                List.of(
                    Map.of(
                        "taskId", "processApplication",
                        "name", "Process Application",
                        "workingDaysAllowed", 2
                    )
                )
            ),
            Arguments.of(
                "submitCase", "caseUnderReview", "anything",
                List.of(
                    Map.of(
                        "taskId", "reviewAppealSkeletonArgument",
                        "name", "Review Appeal Skeleton Argument",
                        "workingDaysAllowed", 2,
                        "processCategories", "caseProgression"
                    )
                )
            ),
            Arguments.of(
                "submitCase", "caseUnderReview", "",
                List.of(
                    Map.of(
                        "taskId", "reviewAppealSkeletonArgument",
                        "name", "Review Appeal Skeleton Argument",
                        "workingDaysAllowed", 2,
                        "processCategories", "caseProgression"
                    )
                )
            ),
            Arguments.of(
                "submitCase", "caseUnderReview", null,
                List.of(
                    Map.of(
                    "taskId", "reviewAppealSkeletonArgument",
                    "name", "Review Appeal Skeleton Argument",
                    "workingDaysAllowed", 2,
                    "processCategories", "caseProgression"
                )
                )
            ),
            Arguments.of(
                "submitTimeExtension", "", null,
                List.of(
                    Map.of(
                    "taskId", "decideOnTimeExtension",
                    "name", "Decide On Time Extension",
                    "workingDaysAllowed", 2,
                    "processCategories", "timeExtension"
                )
                )
            ),
            Arguments.of(
                "requestCaseBuilding", "caseBuilding", null,
                List.of(
                    Map.of(
                    "taskId", "followUpOverdueCaseBuilding",
                    "name", "Follow-up overdue case building",
                    "workingDaysAllowed", 2,
                    "processCategories", "followUpOverdue",
                    "delayDuration", 0
                )
                )
            ),
            Arguments.of(
                "listCma", "cmaListed", null,
                List.of(
                    Map.of(
                    "taskId", "attendCma",
                    "name", "Attend Cma",
                    "workingDaysAllowed", 2,
                    "processCategories", "caseProgression"
                )
                )
            ),
            Arguments.of(
                "uploadHomeOfficeAppealResponse", "respondentReview", "",
                List.of(
                    Map.of(
                    "taskId", "reviewRespondentResponse",
                    "name", "Review Respondent Response",
                    "workingDaysAllowed", 2,
                    "processCategories", "caseProgression"
                )
                )
            ),
            Arguments.of(
                "requestRespondentEvidence", "awaitingRespondentEvidence", "",
                List.of(
                    Map.of(
                    "taskId", "followUpOverdueRespondentEvidence",
                    "name", "Follow-up overdue respondent evidence",
                    "delayDuration", 0,
                    "workingDaysAllowed", 2,
                    "processCategories", "followUpOverdue"
                )
                )
            ),
            Arguments.of(
                "dummyEventForMultipleCategories", "DONE", "anything",
                List.of(
                    Map.of(
                    "taskId", "dummyActivity",
                    "name", "Dummy Activity",
                    "delayDuration", 0,
                    "workingDaysAllowed", 2,
                    "processCategories", "caseProgression,followUpOverdue"
                )
                )
            ),
            Arguments.of(
                "submitAppeal", "appealSubmitted", "anything",
                List.of(
                    Map.of(
                    "taskId", "inspectAppeal",
                    "name", "Inspect Appeal",
                    "workingDaysAllowed", 2,
                    "processCategories", "caseProgression"
                )
                )
            ),
            Arguments.of(
                "createMultipleTasks", "appealCreated", "anything",
                List.of(
                    Map.of(
                        "taskId", "firstTask",
                        "name", "First task",
                        "processCategories", "caseProgression"
                    ),
                    Map.of(
                        "taskId", "secondTask",
                        "name", "Second task",
                        "processCategories", "caseProgression"
                    )
                )
            )

        );
    }

    @Test
    void if_this_test_fails_needs_updating_with_your_changes() {
        //The purpose of this test is to prevent adding new rows without being tested
        DmnDecisionTableImpl logic = (DmnDecisionTableImpl) decision.getDecisionLogic();
        assertThat(logic.getRules().size(), is(13));

    }
}
