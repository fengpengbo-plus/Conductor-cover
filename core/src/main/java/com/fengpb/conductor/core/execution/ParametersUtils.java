package com.fengpb.conductor.core.execution;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.Map.Entry;

@Slf4j
@Component
public class ParametersUtils {

    private ObjectMapper objectMapper = new ObjectMapper();

    private TypeReference<Map<String, Object>> map = new TypeReference<Map<String, Object>>() {};

    public Map<String, Object> replace(Map<String, Object> input, Object json) {
        Object doc;
        if (json instanceof String) {
            doc = JsonPath.parse(json.toString());
        } else {
            doc = json;
        }
        Configuration option = Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS);
        DocumentContext documentContext = JsonPath.parse(doc, option);
        return replace(input, documentContext, null);
    }

    public Object replace(String paramString) {
        Configuration option = Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS);
        DocumentContext documentContext = JsonPath.parse(Collections.emptyMap(), option);
        return replaceVariables(paramString, documentContext, null);
    }

    private Map<String, Object> replace(Map<String, Object> input, DocumentContext documentContext, String taskId) {
        for (Entry<String, Object> e : input.entrySet()) {
            Object value = e.getValue();
            if (value instanceof String) {
                Object replaced = replaceVariableSubList(value.toString(), documentContext, taskId);
                e.setValue(replaced);
            } else if (value instanceof Map) {
                Object replaced = replace((Map<String, Object>) value, documentContext, taskId);
                e.setValue(replaced);
            } else if (value instanceof List) {
                Object replaced = replaceList((List<?>)value, documentContext, taskId);
                e.setValue(replaced);
            } else {
                e.setValue(value);
            }
        }
        return input;
    }

    private Object replaceList(List<?> values, DocumentContext documentContext, String taskId) {
        List<Object> replaceList = new LinkedList<>();
        for (Object listValue:values) {
            if (listValue instanceof String) {
                Object replaced = replaceVariableSubList(listValue.toString(), documentContext, taskId);
                replaceList.add(replaced);
            } else if (listValue instanceof Map) {
                Object replaced = replace((Map<String, Object>) listValue, documentContext, taskId);
                replaceList.add(replaced);
            } else if (listValue instanceof List){
                Object replaced = replaceList((List<?>)listValue, documentContext, taskId);
                replaceList.add(replaced);
            } else {
                replaceList.add(listValue);
            }
        }
        return replaceList;
    }

    private Object replaceVariableSubList(String paramString, DocumentContext documentContext, String taskId) {
        if (paramString.contains("[{")) {
            int startRecursive = paramString.indexOf("[{");
            int endRecursive = paramString.lastIndexOf("]}");
            Object[] convertedValues = new Object[3];
            String[] values = new String[3];
            values[0] = paramString.substring(0, startRecursive + 1);
            values[1] = paramString.substring(startRecursive, endRecursive + 2);
            values[2] = paramString.substring(endRecursive + 1);
            convertedValues[0] = replaceVariables(values[0], documentContext, taskId);
            convertedValues[1] = replaceVariableList(values[1].substring(1, values[1].length() - 1), documentContext, taskId);
            convertedValues[2] = replaceVariables(values[2], documentContext, taskId);
            return convertedValue(convertedValues);
        } else {
            return replaceVariables(paramString, documentContext, taskId);
        }
   }

    private Object replaceVariables(String paramString, DocumentContext documentContext, String taskId) {
        String[] values = paramString.split("(?=\\$\\{)|(?<=\\})");
        Object[] convertedValues = new Object[values.length];
        for (int i = 0; i < values.length; i++) {
            convertedValues[i] = values[i];
            if (values != null && values[i].startsWith("${") && values[i].endsWith("}")) {
                String paramPath = values[i].substring(2, values[i].length() - 1);
                try {
                    convertedValues[i] = documentContext.read(paramPath);
                } catch (Exception e) {
                    log.warn("Error reading documentCOntext for paramPath: {}. Exception: {}", paramPath, e);
                    convertedValues[i] = null;
                }
            }
        }
        return convertedValue(convertedValues);
    }

    private Object replaceVariableList(String paramString, DocumentContext documentContext, String taskId) {
        int startRecursive = paramString.indexOf("[{");
        int endRecursive = paramString.lastIndexOf("}]");
        Object recursive = null;
        if (startRecursive >= 0 && endRecursive > 0) {
            String tmp = paramString.substring(startRecursive + 1, endRecursive + 1);
            paramString = paramString.substring(0, startRecursive + 1) + "$.tmp" + paramString.substring(endRecursive + 1);
            recursive = replaceVariableList(tmp, documentContext, taskId);
        }
        String[] values = paramString.split("(?=\\$\\{)|(?<=\\})");
        int loopTimes = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i].startsWith("${") && values[i].endsWith("}")) {
                String paramPath = values[i].substring(2, values[i].length() - 1);
                if (paramPath.contains("]")) {
                    try {
                        paramPath = paramPath.substring(0, paramPath.indexOf("]") + 1);
                        Object eveResult = documentContext.read(paramPath);
                        log.info(eveResult.getClass().toString());
                        if (!(eveResult instanceof JSONArray)) {
                            loopTimes = 1;
                            break;
                        } else {
                            JSONArray array = (JSONArray) eveResult;
                            loopTimes = array.size() > loopTimes ? array.size() : loopTimes;
                        }
                    } catch (Exception e) {
                        log.warn("Error reading documentContext for paramPath: {}. Exception: {}", paramPath, e);
                    }
                }
            }
        }
        List<String> loopStr = new ArrayList<>();
        for (int i = 0; i < loopTimes; i++) {
            for (int j = 0; j < values.length; j++) {
                loopStr.add(values[j]);
                if (values[j].startsWith("${") && values[j].endsWith("}")) {
                    String paramPath = values[j].substring(2, values[j].length() - 1);
                    if (paramPath.contains("[*]"))
                        paramPath = paramPath.replace("[*]", "[" + i + "]");
                    try {
                        loopStr.set(loopStr.size() - 1, documentContext.read(paramPath));
                    } catch (Exception e) {
                        log.warn("Error reading documentContext for paramPath: {}. Exception: {}", paramPath, e);
                    }
                }
            }
            if (i != loopTimes - 1)
                loopStr.add(",");
        }
        Object convertedResult = convertedValue(loopStr.toArray());
        String result = convertedResult == null ? null : convertedResult.toString();
        if (recursive != null && result != null)
            result = result.replace("$.tmp", recursive.toString());
        return result;
    }

    public Object convertedValue(Object[] convertedValues) {
        Object retObj = null;
        if (convertedValues.length > 0) {
            retObj = convertedValues[0];
            if (convertedValues.length > 1) {
                for (int i = 0; i < convertedValues.length; i++) {
                    Object val = convertedValues[i];
                    if (val == null) {
                        val = "";
                    }
                    if (i == 0) {
                        retObj = val;
                    } else {
                        retObj = retObj + "" + val.toString();
                    }
                }
            }
        }
        return retObj;
    }

    private Map<String, Object> clone(Map<String, Object> inputTemplate) {
        return null;
    }
}
