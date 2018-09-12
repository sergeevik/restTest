package tester.service;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class ResultLogger {
    private Map<String, Result> resultMap;
    private static Logger log = Logger.getLogger(ResultLogger.class);

    public ResultLogger() {
        this.resultMap = new HashMap<>();
    }

    public void saveFail(String reqUrl){
        resultMap.put(reqUrl, Result.FAIL);
    }
    public void saveOk(String reqUrl){
        resultMap.put(reqUrl, Result.OK);
    }

    public void printResultToLog(String headName){
        log.info(headName.toUpperCase() + " RESULTS:");
        for (Map.Entry<String, Result> resultEntry : resultMap.entrySet()) {
            log.info(resultEntry.getValue() + ": " + resultEntry.getKey());
        }
    }

    private enum Result{
        OK,
        FAIL
    }
}
