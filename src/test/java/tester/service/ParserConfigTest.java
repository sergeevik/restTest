package tester.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import tester.model.Properties;
import tester.model.Request;
import tester.model.Scenario;
import tester.model.Scenarios;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ParserConfig.class, ObjectMapper.class, File.class })
public class ParserConfigTest {

    private static final String FILE_URL = "abc";

    @Test
    public void testRequestMapperInvoke() throws Exception {
        ObjectMapper mock = mock(ObjectMapper.class);
        PowerMockito.whenNew(ObjectMapper.class)
                .withNoArguments()
                .thenReturn(mock);
        File fileMock = mock(File.class);
        PowerMockito.whenNew(File.class)
                .withAnyArguments()
                .thenReturn(fileMock);

        ParserConfig.getRequest(FILE_URL);
        Mockito.verify(mock, Mockito.times(1))
                .readValue(fileMock, Request.class);
    }
    @Test
    public void testPropertiesMapperInvoke() throws Exception {
        ObjectMapper mock = mock(ObjectMapper.class);
        PowerMockito.whenNew(ObjectMapper.class)
                .withNoArguments()
                .thenReturn(mock);
        File fileMock = mock(File.class);
        PowerMockito.whenNew(File.class)
                .withAnyArguments()
                .thenReturn(fileMock);

        ParserConfig.getCommonProperties(FILE_URL);
        Mockito.verify(mock, Mockito.times(1))
                .readValue(fileMock, Properties.class);
    }
    @Test
    public void testScenariosMapperInvoke() throws Exception {
        ObjectMapper mock = mock(ObjectMapper.class);
        PowerMockito.whenNew(ObjectMapper.class)
                .withNoArguments()
                .thenReturn(mock);
        File fileMock = mock(File.class);
        PowerMockito.whenNew(File.class)
                .withAnyArguments()
                .thenReturn(fileMock);

        ParserConfig.getScenarios(FILE_URL);
        Mockito.verify(mock, Mockito.times(1))
                .readValue(fileMock, Scenarios.class);
    }

    @Test
    public void testScenariosSort() throws Exception {
        ObjectMapper mock = mock(ObjectMapper.class);
        PowerMockito.whenNew(ObjectMapper.class)
                .withNoArguments()
                .thenReturn(mock);
        File fileMock = mock(File.class);
        PowerMockito.whenNew(File.class)
                .withAnyArguments()
                .thenReturn(fileMock);

        Scenario step1 = new Scenario().withStepId(1);
        Scenario step2 = new Scenario().withStepId(2);
        Scenario step3 = new Scenario().withStepId(3);
        List<Scenario> scenariosToReturn = Arrays.asList(step3, step1, step2);
        Scenarios scenarios = new Scenarios().withScenarios(scenariosToReturn);
        when(mock.readValue(fileMock, Scenarios.class)).thenReturn(scenarios);

        List<Scenario> scenariosExpected = Arrays.asList(step1, step2, step3);
        Scenarios scenariosSortByID = ParserConfig.getScenariosSortByID(FILE_URL);
        List<Scenario> actualScenarios = scenariosSortByID.getScenarios();
        assertThat(actualScenarios).isEqualTo(scenariosExpected);

    }
}