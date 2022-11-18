package com.smartsparrow.graphql.schema;

import static graphql.schema.DataFetchingEnvironmentImpl.newDataFetchingEnvironment;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.graphql.BronteGQLContext;
import com.smartsparrow.graphql.auth.AllowLearnspaceRoles;
import com.smartsparrow.iam.exception.PermissionFault;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.MutableAuthenticationContext;

import io.leangen.graphql.execution.ResolutionEnvironment;

class LearnSchemaTest {

    @InjectMocks
    private LearnSchema learnSchema;
    @Mock
    private AllowLearnspaceRoles allowLearnspaceRoles;
    @Mock
    private AuthenticationContext authenticationContext;
    @Mock
    private MutableAuthenticationContext mutableAuthenticationContext;

    private ResolutionEnvironment resolutionEnvironment;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        resolutionEnvironment = new ResolutionEnvironment(
                null,
                newDataFetchingEnvironment()
                        .context(new BronteGQLContext()
                                         .setMutableAuthenticationContext(mutableAuthenticationContext)
                                         .setAuthenticationContext(authenticationContext)).build(),
                null,
                null,
                null,
                null);
    }

    @Test
    void getLearn_notLearnspaceRole() {
        when(allowLearnspaceRoles.test(authenticationContext)).thenReturn(false);

        assertThrows(PermissionFault.class,() -> learnSchema.getLearn(resolutionEnvironment).join());
    }

    @Test
    void getLearn_LearnspaceRole() {
        when(allowLearnspaceRoles.test(authenticationContext)).thenReturn(true);

        learnSchema.getLearn(resolutionEnvironment)
                        .handle((learn, throwable) -> {
                            assertNotNull(learn);
                            return learn;
                        }).join();
    }
}
