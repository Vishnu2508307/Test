package com.smartsparrow.graphql.schema;

import static com.smartsparrow.competency.DocumentDataStubs.DOCUMENT_ID;
import static com.smartsparrow.competency.DocumentDataStubs.ITEM_A_ID;
import static graphql.schema.DataFetchingEnvironmentImpl.newDataFetchingEnvironment;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.graphql.BronteGQLContext;
import com.smartsparrow.graphql.type.Learn;
import com.smartsparrow.iam.IamTestUtils;
import com.smartsparrow.iam.service.MutableAuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.learner.data.CompetencyMet;
import com.smartsparrow.learner.data.CompetencyMetByStudent;
import com.smartsparrow.learner.payload.LearnerDocumentItemPayload;
import com.smartsparrow.learner.service.CompetencyMetService;

import io.leangen.graphql.execution.ResolutionEnvironment;
import io.leangen.graphql.execution.relay.Page;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class LearnerCompetencyMetSchemaTest {

    @InjectMocks
    private LearnerCompetencyMetSchema learnerCompetencyMetSchema;

    @Mock
    private CompetencyMetService competencyMetService;

    @Mock
    private AuthenticationContextProvider authenticationContextProvider;

    @Mock
    private MutableAuthenticationContext mutableAuthenticationContext;
    private ResolutionEnvironment resolutionEnvironment;


    private final UUID studentId = UUID.randomUUID();

    private Learn learn;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        IamTestUtils.mockAuthenticationContextProvider(authenticationContextProvider, studentId);
        learn = mock(Learn.class);
        resolutionEnvironment = new ResolutionEnvironment(
                null,
                newDataFetchingEnvironment()
                        .context(new BronteGQLContext()
                                         .setMutableAuthenticationContext(mutableAuthenticationContext)
                                         .setAuthenticationContext(authenticationContextProvider.get())).build(),
                null,
                null,
                null,
                null);
    }

    @Test
    void getCompetencyMetByStudent() {

        LearnerDocumentItemPayload payload = mock(LearnerDocumentItemPayload.class);
        when(payload.getDocumentId()).thenReturn(DOCUMENT_ID);
        when(payload.getId()).thenReturn(ITEM_A_ID);
        CompetencyMetByStudent competencyMet = new CompetencyMetByStudent();

        when(competencyMetService.findLatest(studentId, DOCUMENT_ID, ITEM_A_ID)).thenReturn(Mono.just(
                competencyMet
        ));

        CompetencyMetByStudent found = learnerCompetencyMetSchema
                .getCompetencyMetByStudent(resolutionEnvironment, payload)
                .join();

        assertNotNull(found);

        verify(competencyMetService).findLatest(studentId, DOCUMENT_ID, ITEM_A_ID);
    }

    @Test
    void getAllCompetencyMetByDocument() {
        when(competencyMetService.findLatest(studentId, DOCUMENT_ID)).thenReturn(Flux.just(
                new CompetencyMetByStudent()
        ));

        Page<CompetencyMetByStudent> page = learnerCompetencyMetSchema
                .getAllCompetencyMetByDocument(resolutionEnvironment, learn, DOCUMENT_ID, null, null)
                .join();

        assertNotNull(page);
        assertEquals(1, page.getEdges().size());

        verify(competencyMetService).findLatest(studentId, DOCUMENT_ID);
    }

    @Test
    void getCompetencyMet() {
        UUID metId = UUID.randomUUID();
        CompetencyMetByStudent byStudent = new CompetencyMetByStudent()
                .setMetId(metId);
        when(competencyMetService.findCompetencyMet(metId)).thenReturn(Mono.just(new CompetencyMet()));

        CompetencyMet found = learnerCompetencyMetSchema.getCompetencyMet(byStudent).join();

        assertNotNull(found);
        verify(competencyMetService).findCompetencyMet(metId);
    }

    @Test
    void getAllCompetencyMet() {
        when(competencyMetService.findLatest(studentId)).thenReturn(Flux.just(
                new CompetencyMetByStudent()
        ));

        Page<CompetencyMetByStudent> page = learnerCompetencyMetSchema
                .getAllCompetencyMet(resolutionEnvironment, learn, null, null).join();

        assertNotNull(page);
        assertEquals(1, page.getEdges().size());

        verify(competencyMetService).findLatest(studentId);
    }

}
