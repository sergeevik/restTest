package tester.service;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ResultLogger {
    private Map<String, Result> resultMap;
    private static Logger log = Logger.getLogger(ResultLogger.class);
    private String outFile;

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
        StringBuilder builder = new StringBuilder();
        builder.append(headName.toUpperCase())
                .append(" RESULTS:\n");
        for (Map.Entry<String, Result> resultEntry : resultMap.entrySet()) {
            builder.append(resultEntry.getValue())
                    .append(": ")
                    .append(resultEntry.getKey())
                    .append("\n");
        }
        if (outFile != null) {
            try (OutputStream outputStream = new FileOutputStream(outFile)){
                outputStream.write(builder.toString().getBytes());
            } catch (IOException ex) {
                log.warn("Ошибка записи результата в файл.", ex);
            }
        }
        log.info(builder.toString());
    }

    public void setOutFile(String outFile) {
        this.outFile = outFile;
    }

    private enum Result{
        OK,
        FAIL
    }
}
