package uk.gov.hmcts.reform.wataskconfigurationtemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.impl.DefaultDmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.impl.spi.transform.DmnTransformListener;
import org.camunda.bpm.dmn.engine.impl.spi.type.DmnDataTypeTransformer;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.camunda.spin.plugin.variable.type.impl.SpinValueTypeImpl;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.web.client.match.JsonPathRequestMatchers;

import java.io.InputStream;
import java.util.Map;

public abstract class DmnDecisionTableBaseUnitTest {

    protected static DmnDecisionTable CURRENT_DMN_DECISION_TABLE;
    protected DmnEngine dmnEngine;
    protected DmnDecision decision;

    @BeforeEach
    void setUp() {
        DefaultDmnEngineConfiguration defaultDmnEngineConfiguration = (DefaultDmnEngineConfiguration) DmnEngineConfiguration
            .createDefaultDmnEngineConfiguration();

        dmnEngine = defaultDmnEngineConfiguration.buildEngine();

        defaultDmnEngineConfiguration.getTransformer()
            .getDataTypeTransformerRegistry().addTransformer("json", new JsonDataTypeTransformer());

        // Parse decision
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = contextClassLoader.getResourceAsStream(CURRENT_DMN_DECISION_TABLE.getFileName());
        decision = dmnEngine.parseDecision(CURRENT_DMN_DECISION_TABLE.getKey(), inputStream);

    }

    public DmnDecisionTableResult evaluateDmnTable(Map<String, Object> variables) {
        DmnDecisionTableResult dmnDecisionRuleResults = dmnEngine.evaluateDecisionTable(decision, variables);
        return dmnDecisionRuleResults;
    }

    private class JsonDataTypeTransformer implements DmnDataTypeTransformer {
        @Override
        public TypedValue transform(Object value) throws IllegalArgumentException {
            new SpinValueTypeImpl("json").createValue()
            return new ObjectMapper().convertValue(value, new TypeReference<TypedValue>() {
            });
        }
    }
}
