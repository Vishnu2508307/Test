package mercury.glue.step;

import static mercury.glue.wiring.CitrusConfiguration.REST_CLIENT;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.http.client.HttpClient;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class RestSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(REST_CLIENT)
    private HttpClient client;

    @When("^user requests \"([^\"]*)\" resource$")
    public void userRequestsResource(String url){
        runner.http(action -> action.client(client).send().get(url));
    }

    @Then("^user receives \"([^\"]*)\" response$")
    public void userReceivesResponse(String response) {
        runner.http(action -> action.client(client).receive().response(HttpStatus.OK).payload(response));
    }

    @Then("^mercury should respond with http status \"([^\"]*)\"$")
    public void mercuryShouldRespondWithHttpStatus(String httpStatus) {
        runner.http(action -> action.client(REST_CLIENT)
                .receive()
                .response(HttpStatus.valueOf(httpStatus)));
    }

    @Then("^mercury should respond with http status \"(\\d+)\" \"([^\"]*)\" and error message \"([^\"]*)\"$")
    public void mercuryShouldRespondWithHttpStatusAndErrorMessage(int httpStatus, String type, String errorMessage) {
        runner.http(action -> action.client(REST_CLIENT)
                .receive()
                .response().payload("{" +
                        "\"status\":" + httpStatus + "," +
                        "\"type\":\"" + type + "\"," +
                        "\"message\":\"" + errorMessage + "\"" +
                        "}"
                ));
    }
}
