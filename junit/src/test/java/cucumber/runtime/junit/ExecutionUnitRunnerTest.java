package cucumber.runtime.junit;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import gherkin.I18n;
import gherkin.formatter.model.Step;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;

import cucumber.runtime.Glue;
import cucumber.runtime.StepDefinition;
import cucumber.runtime.StepDefinitionMatch;
import cucumber.runtime.io.ClasspathResourceLoader;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.CucumberScenario;

public class ExecutionUnitRunnerTest {
	
	@Mock
	cucumber.runtime.Runtime runtime;
	
	@Mock
	Glue glue;
	
	@Mock
	StepDefinitionMatch stepMatch;
	
	@Mock
	StepDefinition stepDef;
	
	@Before
	public void initMocks() throws Exception {
		MockitoAnnotations.initMocks(this);
		mockStepDefinitions();
	}
	
    private void mockStepDefinitions() throws Exception {
    	when(stepMatch.getStepDefinition()).thenReturn(stepDef);
    	when(stepDef.getMethod()).thenReturn(StepDefinition.class.getMethod("getMethod"));
    	when(glue.stepDefinitionMatch(anyString(), any(Step.class), any(I18n.class)))
    		.thenReturn(stepMatch);
    	when(runtime.getGlue()).thenReturn(glue);
	}

	@Test
    public void shouldAssignUnequalDescriptionsToDifferentOccurrencesOfSameStepInAScenario() throws Exception {
   	
        List<CucumberFeature> features = CucumberFeature.load(
                new ClasspathResourceLoader(this.getClass().getClassLoader()),
                asList("cucumber/runtime/junit/fb.feature"),
                Collections.emptyList()
        );

        ExecutionUnitRunner runner = new ExecutionUnitRunner(
        		runtime,
                (CucumberScenario) features.get(0).getFeatureElements().get(0),
                null
        );

        // fish out the two occurrences of the same step and check whether we really got them
        Step stepOccurrence1 = runner.getChildren().get(0);
        Step stepOccurrence2 = runner.getChildren().get(2);
        assertEquals(stepOccurrence1.getName(), stepOccurrence2.getName());

        // then check that the descriptions are unequal
        Description runnerDescription = runner.getDescription();

        Description stepDescription1 = runnerDescription.getChildren().get(0);
        Description stepDescription2 = runnerDescription.getChildren().get(2);

        assertFalse("Descriptions must not be equal.", stepDescription1.equals(stepDescription2));
    }
	
    @Test
    public void shouldAssignScenarioNameWhenStepDefNotFound() throws Exception {
    	//Simulates no step definition written yet
    	when(glue.stepDefinitionMatch(anyString(), any(Step.class), any(I18n.class)))
    		.thenReturn(null);
    	when(runtime.getGlue()).thenReturn(glue);
    	verify(stepDef, never()).getMethod();
    	verify(stepMatch, never()).getStepDefinition();
        List<CucumberFeature> features = CucumberFeature.load(
                new ClasspathResourceLoader(this.getClass().getClassLoader()),
                asList("cucumber/runtime/junit/feature_with_same_steps_in_different_scenarios.feature"),
                Collections.emptyList()
        );

        ExecutionUnitRunner runner = new ExecutionUnitRunner(
                runtime,
                (CucumberScenario) features.get(0).getFeatureElements().get(0),
                null
        );

        // fish out the data from runner
        Description runnerDescription = runner.getDescription();
        Description stepDescription = runnerDescription.getChildren().get(0);

        assertEquals("description includes scenario name as class name", "Scenario: first", stepDescription.getClassName());
    }

    @Test
    public void shouldIncludeScenarioNameAsClassNameInStepDescriptions() throws Exception {
        List<CucumberFeature> features = CucumberFeature.load(
                new ClasspathResourceLoader(this.getClass().getClassLoader()),
                asList("cucumber/runtime/junit/feature_with_same_steps_in_different_scenarios.feature"),
                Collections.emptyList()
        );

        ExecutionUnitRunner runner = new ExecutionUnitRunner(
                runtime,
                (CucumberScenario) features.get(0).getFeatureElements().get(0),
                null
        );

        // fish out the data from runner
        Step step = runner.getChildren().get(0);
        Description runnerDescription = runner.getDescription();
        Description stepDescription = runnerDescription.getChildren().get(0);

        assertEquals("description includes scenario name as class name", "cucumber.runtime.StepDefinition", stepDescription.getClassName());
        assertEquals("description includes step keyword and name as method name", step.getKeyword() + step.getName(), stepDescription.getMethodName());
    }
}
