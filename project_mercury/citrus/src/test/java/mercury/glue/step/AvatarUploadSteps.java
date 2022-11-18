package mercury.glue.step;

import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.validation.json.JsonMappingValidationCallback;
import com.google.common.io.BaseEncoding;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.util.Images;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;

public class AvatarUploadSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("^\"([^\"]*)\" uploads avatar$")
    public void uploadsAvatar(String accountName) throws Throwable {
        authenticationSteps.authenticateUser(accountName);

        String image = loadImageAsString("avatar/original.png");

        PayloadBuilder payload = new PayloadBuilder();
        payload.addField("type", "me.avatar.set");
        payload.addField("avatar", "data:image/png;base64," + image);
        messageOperations.sendJSON(runner, payload.build());

        messageOperations.validateResponseType(runner, "me.avatar.set.ok");
    }

    @When("^\"([^\"]*)\" uploads broken avatar image$")
    public void uploadsBrokenAvatarImage(String accountName) throws Throwable {
        authenticationSteps.authenticateUser(accountName);

        PayloadBuilder payload = new PayloadBuilder();
        payload.addField("type", "me.avatar.set");
        payload.addField("avatar", "data:image/gif;base64,R0lGODlhAQABAIAAAAAAAPyH5BAEAAAAALAAAAAABAAEAAAIBRAA7");
        messageOperations.sendJSON(runner, payload.build());
    }

    @When("^\"([^\"]*)\" uploads empty avatar$")
    public void uploadsEmptyAvatar(String accountName) throws Throwable {
        authenticationSteps.authenticateUser(accountName);

        PayloadBuilder payload = new PayloadBuilder();
        payload.addField("type", "me.avatar.set");
        messageOperations.sendJSON(runner, payload.build());
    }

    @SuppressWarnings("unchecked")
    @Then("^me.get message returns smallAvatar$")
    public void meGetMessageReturnsSmallAvatar() throws IOException, Throwable {
        PayloadBuilder payload = new PayloadBuilder();
        payload.addField("type", "me.get");
        messageOperations.sendJSON(runner, payload.build());

        BufferedImage expected = loadImage("avatar/small.png");

        messageOperations.validateResponseType(runner, "me.get.ok",
                action -> action.validationCallback(new JsonMappingValidationCallback<BasicResponseMessage>(BasicResponseMessage.class) {
                    @Override
                    public void validate(BasicResponseMessage payload, Map<String, Object> headers, TestContext context) {
                        Map<String, Object> accountPayload = (Map<String, Object>) payload.getResponse().get("account");
                        String actualAvatar = (String) accountPayload.get("avatarSmall");
                        assertEquals("image/png", Images.parse(actualAvatar).getMimeType(), "Small avatar should have image/png content type");
                        byte[] decoded = BaseEncoding.base64().decode(Images.parse(actualAvatar).getData());
                        try {
                            assertTrue(Images.isSameImage(expected, ImageIO.read(new ByteArrayInputStream(decoded))));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }));
    }

    private BufferedImage loadImage(String name) throws IOException {
        return ImageIO.read(getClass().getClassLoader().getResourceAsStream(name));
    }

    private String loadImageAsString(String name) throws IOException {
        byte[] bytes = loadImageAsBytes(name);
        return BaseEncoding.base64().encode(bytes);
    }

    private byte[] loadImageAsBytes(String name) throws IOException {
        return IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream(name));
    }
}
