package mercury.helpers.collaborator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.consol.citrus.context.TestContext;

import mercury.common.ResponseMessageValidationCallback;

public class CollaboratorHelper {

    @Nonnull
    public static ResponseMessageValidationCallback<Object> validateCollaboratorsResponse(Map<String, String> collaborators, final String type) {
        return new ResponseMessageValidationCallback<Object>(Object.class) {
            @Override
            public void validate(Object payload, Map<String, Object> headers, TestContext context) {
                List<String> accountColl = collaborators.entrySet().stream()
                        .filter(one-> one.getValue().trim().equals("account"))
                        .map(Map.Entry::getKey).collect(Collectors.toList());

                List<String> teamColl = collaborators.entrySet().stream()
                        .filter(one-> one.getValue().trim().equals("team"))
                        .map(Map.Entry::getKey).collect(Collectors.toList());

                assertNotNull(accountColl);
                assertNotNull(teamColl);

                Map<String, Object> collaborators = (Map<String, Object>) payload;

                List<Object> actualAccountCollaborators = (List<Object>) collaborators.get("accounts");
                List<Object> actualTeamCollaborators = (List<Object>) collaborators.get("teams");

                if (!accountColl.isEmpty()) {
                    assertEquals(accountColl.size(), actualAccountCollaborators.size());
                }

                if (!teamColl.isEmpty()) {
                    assertEquals(teamColl.size(), actualTeamCollaborators.size());
                }

            }

            @Override
            public String getRootElementName() {
                return "collaborators";
            }

            @Override
            public String getType() {
                return type;
            }
        };
    }
}
