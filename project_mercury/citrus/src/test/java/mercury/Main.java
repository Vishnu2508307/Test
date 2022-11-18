package mercury;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.SnippetType;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(plugin = { "com.consol.citrus.cucumber.CitrusReporter", "pretty" }, snippets = SnippetType.CAMELCASE)
public class Main {

}
