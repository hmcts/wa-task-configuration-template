package uk.gov.hmcts.reform.wataskconfigurationtemplate.dmn;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ConvertMapToList {

    public static void main(String[] args) {
        Map<String, String> map = Map.of(
            "additionalProperties_key1","value1",
            "additionalProperties_key2","value2",
            "additionalProperties_key3","value3",
            "additionalProperties_key4","value4",
            "additionalProperties_key5","value5"
        );

        Map<String, String> output = map.entrySet().stream()
            .filter(e -> e.getKey().contains("additionalProperties_"))
            .collect(Collectors.toMap(
                e -> e.getKey().replace("additionalProperties_", ""),
                Map.Entry::getValue
            ));


        Map<String, Map<String, String>> additionalProperties = new HashMap<>();
        additionalProperties.put("additionalProperties", output);
        System.out.println(additionalProperties);
    }
}
