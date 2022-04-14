package uk.gov.hmcts.reform.wataskconfigurationtemplate.dmn;

import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableImpl;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.wataskconfigurationtemplate.DmnDecisionTableBaseUnitTest;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.reform.wataskconfigurationtemplate.DmnDecisionTable.WA_TASK_PERMISSIONS_WA_WACASETYPE;

class CamundaTaskWaPermissionTest extends DmnDecisionTableBaseUnitTest {

    @BeforeAll
    public static void initialization() {
        CURRENT_DMN_DECISION_TABLE = WA_TASK_PERMISSIONS_WA_WACASETYPE;
    }

    static Stream<Arguments> scenarioProvider() {

        return Stream.of(
            Arguments.of(
                "anything",
                asList(
                    Map.of(
                        "autoAssignable", false,
                        "name", "task-supervisor",
                        "value", "Read,Refer,Manage,Cancel",
                        "caseAccessCategory", "categoryA"
                    ),
                    Map.of(
                        "name", "tribunal-caseworker",
                        "value", "Read,Refer,Own,Manage,Cancel",
                        "roleCategory", "LEGAL_OPERATIONS",
                        "autoAssignable", false
                    ),
                    Map.of(
                        "name", "senior-tribunal-caseworker",
                        "value", "Read,Refer,Own,Manage,Cancel",
                        "roleCategory", "LEGAL_OPERATIONS",
                        "autoAssignable", false
                    )
                )
            ),
            Arguments.of(
                "null",
                asList(
                    Map.of(
                        "autoAssignable", false,
                        "name", "task-supervisor",
                        "value", "Read,Refer,Manage,Cancel",
                        "caseAccessCategory", "categoryA"
                    ),
                    Map.of(
                        "name", "tribunal-caseworker",
                        "value", "Read,Refer,Own,Manage,Cancel",
                        "roleCategory", "LEGAL_OPERATIONS",
                        "autoAssignable", false
                    ),
                    Map.of(
                        "name", "senior-tribunal-caseworker",
                        "value", "Read,Refer,Own,Manage,Cancel",
                        "roleCategory", "LEGAL_OPERATIONS",
                        "autoAssignable", false
                    )
                )
            )
        );
    }

    @ParameterizedTest(name = "case data: {0}")
    @MethodSource("scenarioProvider")
    void given_multiple_event_ids_should_evaluate_dmn(String caseData,
                                                      List<Map<String, String>> expectation) {

        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("case", caseData);

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        MatcherAssert.assertThat(dmnDecisionTableResult.getResultList(), is(expectation));
    }

    @SuppressWarnings("checkstyle:indentation")
    @Test
    void given_processApplication_taskType_when_evaluate_dmn_then_it_returns_expected_rule() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("taskAttributes", Map.of("taskType", "processApplication"));

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);

        MatcherAssert.assertThat(dmnDecisionTableResult.getResultList(), is(List.of(
            Map.of(
                "name", "task-supervisor",
                "value", "Read,Refer,Manage,Cancel",
                "autoAssignable", false,
                "caseAccessCategory", "categoryA"
            ),
            Map.of(
                "name", "tribunal-caseworker",
                "value", "Read,Refer,Execute",
                "roleCategory", "LEGAL_OPERATIONS",
                "assignmentPriority", 2,
                "autoAssignable", false,
                "caseAccessCategory", "categoryA,categoryB"
            ),
            Map.of(
                "name", "senior-tribunal-caseworker",
                "value", "Read,Refer,Execute",
                "roleCategory", "LEGAL_OPERATIONS",
                "assignmentPriority", 2,
                "autoAssignable", false,
                "caseAccessCategory", "categoryC,categoryD"
            )
        )));
    }

    @SuppressWarnings("checkstyle:indentation")
    @Test
    void given_reviewSpecificAccessRequestJudiciary_taskType_when_evaluate_dmn_then_it_returns_expected_rule() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("taskAttributes", Map.of("taskType", "reviewSpecificAccessRequestJudiciary"));

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);

        MatcherAssert.assertThat(dmnDecisionTableResult.getResultList(), is(List.of(
            Map.of(
                "name", "task-supervisor",
                "value", "Read,Refer,Manage,Cancel",
                "autoAssignable", false,
                "caseAccessCategory", "categoryA"
            ),
            Map.of(
                "name", "judge",
                "value", "Read,Refer,Own",
                "roleCategory", "JUDICIAL",
                "assignmentPriority", 1,
                "autoAssignable", true
            )
        )));
    }

    @SuppressWarnings("checkstyle:indentation")
    @Test
    void given_reviewSpecificAccessRequestLegalOps_taskType_when_evaluate_dmn_then_it_returns_expected_rule() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("taskAttributes", Map.of("taskType", "reviewSpecificAccessRequestLegalOps"));

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);

        MatcherAssert.assertThat(dmnDecisionTableResult.getResultList(), is(List.of(
            Map.of(
                "name", "task-supervisor",
                "value", "Read,Refer,Manage,Cancel",
                "autoAssignable", false,
                "caseAccessCategory", "categoryA"
            ),
            Map.of(
                "name", "tribunal-caseworker",
                "value", "Read,Refer,Own",
                "roleCategory", "LEGAL_OPERATIONS",
                "assignmentPriority", 1,
                "autoAssignable", true
            ),
            Map.of(
                "name", "senior-tribunal-caseworker",
                "value", "Read,Refer,Own",
                "roleCategory", "LEGAL_OPERATIONS",
                "assignmentPriority", 1,
                "autoAssignable", true
            )
        )));
    }

    @SuppressWarnings("checkstyle:indentation")
    @Test
    void given_reviewSpecificAccessRequestAdmin_taskType_when_evaluate_dmn_then_it_returns_expected_rule() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("taskAttributes", Map.of("taskType", "reviewSpecificAccessRequestAdmin"));

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);

        MatcherAssert.assertThat(dmnDecisionTableResult.getResultList(), is(List.of(
            Map.of(
                "name", "task-supervisor",
                "value", "Read,Refer,Manage,Cancel",
                "autoAssignable", false,
                "caseAccessCategory", "categoryA"
            ),
            Map.of(
                "name", "national-business-centre",
                "value", "Read,Refer,Own",
                "roleCategory", "ADMIN",
                "assignmentPriority", 1,
                "autoAssignable", false
            ),
            Map.of(
                "name", "hearing-centre-admin",
                "value", "Read,Refer,Own",
                "roleCategory", "ADMIN",
                "assignmentPriority", 1,
                "autoAssignable", false
            )
        )));
    }

    @Test
    void if_this_test_fails_needs_updating_with_your_changes() {
        //The purpose of this test is to prevent adding new rows without being tested
        DmnDecisionTableImpl logic = (DmnDecisionTableImpl) decision.getDecisionLogic();
        assertThat(logic.getRules().size(), is(10));

    }
}
