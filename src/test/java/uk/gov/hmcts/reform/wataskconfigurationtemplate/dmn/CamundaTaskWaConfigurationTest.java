package uk.gov.hmcts.reform.wataskconfigurationtemplate.dmn;

import lombok.Builder;
import lombok.Value;
import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableImpl;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.commons.util.StringUtils;
import uk.gov.hmcts.reform.wataskconfigurationtemplate.DmnDecisionTableBaseUnitTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.wataskconfigurationtemplate.DmnDecisionTable.WA_TASK_CONFIGURATION_WA_WACASETYPE;

class CamundaTaskWaConfigurationTest extends DmnDecisionTableBaseUnitTest {

    @BeforeAll
    public static void initialization() {
        CURRENT_DMN_DECISION_TABLE = WA_TASK_CONFIGURATION_WA_WACASETYPE;
    }

    @Test
    void if_this_test_fails_needs_updating_with_your_changes() {
        //The purpose of this test is to prevent adding new rows without being tested
        DmnDecisionTableImpl logic = (DmnDecisionTableImpl) decision.getDecisionLogic();
        assertThat(logic.getRules().size(), is(20));
    }

    @SuppressWarnings("checkstyle:indentation")
    @ParameterizedTest
    @CsvSource(value = {
        "refusalOfHumanRights, Human rights",
        "refusalOfEu, EEA",
        "deprivation, DoC",
        "protection, Protection",
        "revocationOfProtection, Revocation",
        "NULL_VALUE, ''",
        "'', ''"
    }, nullValues = "NULL_VALUE")
    void when_caseData_then_return_expected_appealType(String appealType, String expectedAppealType) {
        VariableMap inputVariables = new VariableMapImpl();
        Map<String, Object> caseData = new HashMap<>(); // allow null values
        caseData.put("appealType", appealType);
        inputVariables.putValue("caseData", caseData);
        inputVariables.putValue("dueDate", "2022-01-01");

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);

        assertTrue(dmnDecisionTableResult.getResultList().contains(Map.of(
            "name", "appealType",
            "value", expectedAppealType,
            "Can reconfigure?", true
        )));

        assertTrue(dmnDecisionTableResult.getResultList().contains(Map.of(
            "name", "caseManagementCategory",
            "value", expectedAppealType,
            "Can reconfigure?", true
        )));

        assertTrue(dmnDecisionTableResult.getResultList().contains(Map.of(
            "name", "priorityDate",
            "value", "2022-01-01",
            "Can reconfigure?", true
        )));
    }

    @Test
    void when_caseData_then_return_expected_case_management_category() {
        String refusalOfEuLabel = "Refusal of application under the EEA regulations";
        String revocationLabel = "Revocation of a protection status";
        List<Map<String, Object>> caseManagementCategories = List.of(
            Map.of(
                "value",
                Map.of("code", "refusalOfHumanRights", "label", "Refusal of a human rights claim"),
                "list_items",
                List.of(Map.of("code", "refusalOfHumanRights", "label", "Refusal of a human rights claim"))
            ),
            Map.of(
                "value", Map.of("code", "refusalOfEu", "label", "Refusal of application under the EEA regulations"),
                "list_items", List.of(Map.of("code", "refusalOfEu", "label", refusalOfEuLabel))
            ),
            Map.of(
                "value", Map.of("code", "deprivation", "label", "Deprivation of citizenship"),
                "list_items", List.of(Map.of("code", "deprivation", "label", "Deprivation of citizenship"))
            ),
            Map.of(
                "value", Map.of("code", "protection", "label", "Refusal of protection claim"),
                "list_items", List.of(Map.of("code", "protection", "label", "Refusal of protection claim"))
            ),
            Map.of(
                "value", Map.of("code", "revocationOfProtection", "label", "Revocation of a protection status"),
                "list_items", List.of(Map.of("code", "revocationOfProtection", "label", revocationLabel))
            )
        );

        List<String> expectedCaseManagementCategories = List.of(
            "Human rights",
            "EEA",
            "DoC",
            "Protection",
            "Revocation"
        );

        for (int i = 0; i < caseManagementCategories.size(); i++) {
            Map<String, Object> caseManagementCategory = caseManagementCategories.get(i);
            Map<String, Object> caseData = new HashMap<>(); // allow null values
            caseData.put("caseManagementCategory", caseManagementCategory);
            VariableMap inputVariables = new VariableMapImpl();
            inputVariables.putValue("caseData", caseData);

            DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);

            assertTrue(dmnDecisionTableResult.getResultList().contains(Map.of(
                "name", "caseManagementCategory",
                "value", expectedCaseManagementCategories.get(i),
                "Can reconfigure?", true
            )));
        }
    }

    @ParameterizedTest
    @MethodSource("nameAndValueScenarioProvider")
    void when_caseData_then_return_expected_name_and_value_rows(Scenario scenario) {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("caseData", scenario.caseData);
        inputVariables.putValue("taskAttributes", scenario.taskAttributes);

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        assertThat(dmnDecisionTableResult.getResultList(), is(getExpectedValues(scenario)));
    }

    private static Stream<Scenario> nameAndValueScenarioProvider() {
        Scenario givenCaseDataIsMissedThenDefaultToTaylorHouseScenario = Scenario.builder()
            .caseData(emptyMap())
            .expectedCaseNameValue(null)
            .expectedAppealTypeValue("")
            .expectedRegionValue("1")
            .expectedLocationValue("765324")
            .expectedLocationNameValue("Taylor House")
            .expectedCaseManagementCategoryValue("")
            .expectedDescription("")
            .expectedAdditionalPropertiesKey1(null)
            .expectedAdditionalPropertiesKey2(null)
            .expectedAdditionalPropertiesKey3(null)
            .expectedAdditionalPropertiesKey4(null)
            .expectedPriorityDate("")
            .expectedMinorPriority("500")
            .expectedMajorPriority("5000")
            .build();
        String refusalOfEuLabel = "Refusal of a human rights claim";
        Scenario givenCaseDataIsPresentThenReturnNameAndValueScenario = Scenario.builder()
            .caseData(Map.of(
                "appealType", "refusalOfHumanRights",
                "appellantGivenNames", "some appellant given names",
                "appellantFamilyName", "some appellant family name",
                "caseManagementLocation", Map.of(
                    "region", "some other region",
                    "baseLocation", "some other location"
                ),
                "staffLocation", "some other location name",
                "caseManagementCategory", Map.of(
                    "value", Map.of("code", "refusalOfHumanRights", "label", "Refusal of a human rights claim"),
                    "list_items", List.of(Map.of("code", "refusalOfHumanRights", "label", refusalOfEuLabel))
                )
            ))
            .taskAttributes(Map.of("taskType", "processApplication"))
            .expectedCaseNameValue("some appellant given names some appellant family name")
            .expectedAppealTypeValue("Human rights")
            .expectedRegionValue("some other region")
            .expectedLocationValue("some other location")
            .expectedLocationNameValue("some other location name")
            .expectedCaseManagementCategoryValue("Human rights")
            .expectedWorkType("hearing_work")
            .expectedRoleCategory("LEGAL_OPERATIONS")
            .expectedDescription("[Decide an application]"
                                     + "(/case/WA/WaCaseType/${[CASE_REFERENCE]}/trigger/decideAnApplication)")
            .expectedAdditionalPropertiesKey1("value1")
            .expectedAdditionalPropertiesKey2("value2")
            .expectedAdditionalPropertiesKey3("value3")
            .expectedAdditionalPropertiesKey4("value4")
            .expectedPriorityDate("")
            .expectedMinorPriority("500")
            .expectedMajorPriority("5000")
            .build();

        return Stream.of(
            givenCaseDataIsMissedThenDefaultToTaylorHouseScenario,
            givenCaseDataIsPresentThenReturnNameAndValueScenario
        );
    }

    void when_casaDate_hearingDate_then_return_expected_priority_Date(String appealType, String expectedAppealType) {
        Map<String, Object> caseData = new HashMap<>(); // allow null values
        caseData.put("appealType", appealType);
        caseData.put("nextHearingDate", "2023-01-01");
        caseData.put("urgent", "Yes");
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("caseData", caseData);
        inputVariables.putValue("dueDate", "2022-01-01");


        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);

        assertTrue(dmnDecisionTableResult.getResultList().contains(Map.of(
            "name", "priorityDate",
            "value", "2023-01-01",
            "Can reconfigure?", true
        )));

        assertTrue(dmnDecisionTableResult.getResultList().contains(Map.of(
            "name", "minorPriority",
            "value", "500",
            "Can reconfigure?", true
        )));

        assertTrue(dmnDecisionTableResult.getResultList().contains(Map.of(
            "name", "majorPriority",
            "value", "1000",
            "Can reconfigure?", true
        )));
    }

    void when_no_casaDate_hearingDate_then_return_expected_priority_Date(String appealType, String expectedAppealType) {
        VariableMap inputVariables = new VariableMapImpl();
        Map<String, Object> caseData = new HashMap<>(); // allow null values
        caseData.put("appealType", appealType);
        caseData.put("urgent", "No");
        inputVariables.putValue("caseData", caseData);
        inputVariables.putValue("dueDate", "2022-01-01");


        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);

        assertTrue(dmnDecisionTableResult.getResultList().contains(Map.of(
            "name", "priorityDate",
            "value", "2022-01-01",
            "Can reconfigure?", true
        )));

        assertTrue(dmnDecisionTableResult.getResultList().contains(Map.of(
            "name", "minorPriority",
            "value", "500",
            "Can reconfigure?", true
        )));

        assertTrue(dmnDecisionTableResult.getResultList().contains(Map.of(
            "name", "majorPriority",
            "value", "1000",
            "Can reconfigure?", true
        )));
    }

    @ParameterizedTest
    @CsvSource({
        "reviewSpecificAccessRequestJudiciary", "reviewSpecificAccessRequestLegalOps",
        "reviewSpecificAccessRequestAdmin"
    })
    void when_taskId_then_return_Access_requests(String taskType) {
        VariableMap inputVariables = new VariableMapImpl();

        inputVariables.putValue("taskAttributes", Map.of("taskType", taskType));

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);

        List<Map<String, Object>> workTypeResultList = dmnDecisionTableResult.getResultList().stream()
            .filter((r) -> r.containsValue("workType"))
            .collect(Collectors.toList());

        assertThat(workTypeResultList.size(), is(1));

        assertTrue(workTypeResultList.contains(Map.of(
            "name", "workType",
            "value", "access_requests",
            "Can reconfigure?", true
        )));
    }

    @ParameterizedTest
    @CsvSource({
        "processApplication", "reviewSpecificAccessRequestLegalOps"
    })
    void when_taskId_then_return_Legal_Operations(String taskType) {
        VariableMap inputVariables = new VariableMapImpl();

        inputVariables.putValue("taskAttributes", Map.of("taskType", taskType));

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);

        List<Map<String, Object>> workTypeResultList = dmnDecisionTableResult.getResultList().stream()
            .filter((r) -> r.containsValue("roleCategory"))
            .collect(Collectors.toList());

        assertThat(workTypeResultList.size(), is(1));

        assertTrue(workTypeResultList.contains(Map.of(
            "name", "roleCategory",
            "value", "LEGAL_OPERATIONS",
            "Can reconfigure?", true
        )));
    }

    @ParameterizedTest
    @CsvSource({
        "reviewSpecificAccessRequestJudiciary", "reviewSpecificAccessRequestLegalOps",
        "reviewSpecificAccessRequestAdmin"
    })
    void when_taskId_then_return_assignmentId(String taskType) {
        VariableMap inputVariables = new VariableMapImpl();

        inputVariables.putValue("taskAttributes", Map.of("taskType", taskType));

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);

        List<Map<String, Object>> workTypeResultList = dmnDecisionTableResult.getResultList().stream()
            .filter((r) -> r.containsValue("additionalProperties_roleAssignmentId"))
            .collect(Collectors.toList());

        assertThat(workTypeResultList.size(), is(1));

        assertTrue(workTypeResultList.contains(Map.of(
            "name", "additionalProperties_roleAssignmentId",
            "value", "assignmentId",
            "Can reconfigure?", true
        )));
    }

    @Test
    void when_taskId_then_return_Admin() {
        VariableMap inputVariables = new VariableMapImpl();

        inputVariables.putValue("taskAttributes", Map.of("taskType", "reviewSpecificAccessRequestAdmin"));

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);

        List<Map<String, Object>> workTypeResultList = dmnDecisionTableResult.getResultList().stream()
            .filter((r) -> r.containsValue("roleCategory"))
            .collect(Collectors.toList());

        assertThat(workTypeResultList.size(), is(1));

        assertTrue(workTypeResultList.contains(Map.of(
            "name", "roleCategory",
            "value", "ADMIN",
            "Can reconfigure?", true
        )));
    }

    @Test
    void when_taskId_then_return_Judical() {
        VariableMap inputVariables = new VariableMapImpl();

        inputVariables.putValue("taskAttributes", Map.of("taskType", "reviewSpecificAccessRequestJudiciary"));

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);

        List<Map<String, Object>> workTypeResultList = dmnDecisionTableResult.getResultList().stream()
            .filter((r) -> r.containsValue("roleCategory"))
            .collect(Collectors.toList());

        assertThat(workTypeResultList.size(), is(1));

        assertTrue(workTypeResultList.contains(Map.of(
            "name", "roleCategory",
            "value", "JUDICIAL",
            "Can reconfigure?", true
        )));
    }

    @Value
    @Builder
    private static class Scenario {
        Map<String, Object> caseData;
        Map<String, Object> taskAttributes;
        String expectedCaseNameValue;
        String expectedAppealTypeValue;
        String expectedRegionValue;
        String expectedLocationValue;
        String expectedLocationNameValue;
        String expectedCaseManagementCategoryValue;
        String expectedWorkType;
        String expectedRoleCategory;
        String expectedDescription;
        String expectedAdditionalPropertiesKey1;
        String expectedAdditionalPropertiesKey2;
        String expectedAdditionalPropertiesKey3;
        String expectedAdditionalPropertiesKey4;
        String expectedPriorityDate;
        String expectedMinorPriority;
        String expectedMajorPriority;
    }

    private List<Map<String, Object>> getExpectedValues(Scenario scenario) {
        List<Map<String, Object>> rules = new ArrayList<>();

        getExpectedValue(rules, "caseName", scenario.getExpectedCaseNameValue());
        getExpectedValue(rules, "appealType", scenario.getExpectedAppealTypeValue());
        getExpectedValue(rules, "region", scenario.getExpectedRegionValue());
        getExpectedValue(rules, "location", scenario.getExpectedLocationValue());
        getExpectedValue(rules, "locationName", scenario.getExpectedLocationNameValue());
        getExpectedValue(rules, "caseManagementCategory", scenario.getExpectedCaseManagementCategoryValue());

        if (!Objects.isNull(scenario.getTaskAttributes())
            && StringUtils.isNotBlank(scenario.taskAttributes.get("taskType").toString())) {
            getExpectedValue(rules, "workType", scenario.getExpectedWorkType());
            getExpectedValue(rules, "roleCategory", scenario.getExpectedRoleCategory());
        }
        getExpectedValue(rules, "description", scenario.getExpectedDescription());

        Optional.ofNullable(scenario.getExpectedAdditionalPropertiesKey1())
            .ifPresent(key -> getExpectedValue(rules, "additionalProperties_key1", key));
        Optional.ofNullable(scenario.getExpectedAdditionalPropertiesKey2())
            .ifPresent(key -> getExpectedValue(rules, "additionalProperties_key2", key));
        Optional.ofNullable(scenario.getExpectedAdditionalPropertiesKey3())
            .ifPresent(key -> getExpectedValue(rules, "additionalProperties_key3", key));
        Optional.ofNullable(scenario.getExpectedAdditionalPropertiesKey4())
            .ifPresent(key -> getExpectedValue(rules, "additionalProperties_key4", key));

        getExpectedValue(rules, "priorityDate", scenario.getExpectedPriorityDate());
        getExpectedValue(rules, "minorPriority", scenario.getExpectedMinorPriority());
        getExpectedValue(rules, "majorPriority", scenario.getExpectedMajorPriority());
        return rules;
    }

    private void getExpectedValue(List<Map<String, Object>> rules, String name, String value) {
        Map<String, Object> rule = new HashMap<>();
        rule.put("name", name);
        rule.put("value", value);
        rule.put("Can reconfigure?", true);
        rules.add(rule);
    }

}

