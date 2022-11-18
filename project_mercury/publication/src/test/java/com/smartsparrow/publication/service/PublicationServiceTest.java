package com.smartsparrow.publication.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.UUID;

import com.datastax.driver.core.utils.UUIDs;
import com.smartsparrow.publication.data.ActivityPublicationStatus;
import com.smartsparrow.publication.data.PublicationGateway;
import com.smartsparrow.publication.data.PublicationMetadata;
import com.smartsparrow.publication.data.PublicationMetadataByPublishedActivity;
import com.smartsparrow.publication.data.PublicationOculusData;
import com.smartsparrow.publication.data.PublicationOutputType;
import com.smartsparrow.publication.data.PublicationPayload;
import com.smartsparrow.publication.data.PublicationSummary;
import com.smartsparrow.publication.data.PublishedActivity;
import com.smartsparrow.publication.data.PublicationByExport;
import com.smartsparrow.publication.data.ActivityByAccount;
import com.smartsparrow.publication.data.PublicationActivityPayload;
import com.smartsparrow.publication.data.PublicationByActivity;
import com.smartsparrow.publication.job.data.JobByPublication;
import com.smartsparrow.publication.job.data.JobGateway;
import com.smartsparrow.publication.job.data.JobSummary;
import com.smartsparrow.publication.job.data.Notification;
import com.smartsparrow.publication.job.enums.ArtifactType;
import com.smartsparrow.publication.job.enums.JobType;
import com.smartsparrow.publication.job.enums.NotificationStatus;
import com.smartsparrow.publication.job.enums.NotificationType;
import com.smartsparrow.publication.job.enums.JobStatus;

import org.joda.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.iam.payload.AccountPayload;
import com.smartsparrow.iam.service.AccountService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class PublicationServiceTest {

    @InjectMocks
    private PublicationService publicationService;

    @Mock
    private AccountService accountService;

    @Mock
    private PublicationGateway publicationGateway;

    @Mock
    private JobGateway jobGateway;

    @Mock
    private PublicationBroker publicationBroker;

    private static final UUID publicationID = UUID.randomUUID();
    private static final UUID activityID = UUID.randomUUID();
    private static final UUID accountID = UUID.randomUUID();
    private static final UUID exportID = UUID.randomUUID();
    private static final String title = "title";
    private static final String author = "foo";
    private static final String description = "desc";
    private static final String version = "first edition";
    private static final String config = "";
    private static final String etextVersion = "v1";
    private static final String bookId = "BRNT-TEST";
    private static final UUID jobID = UUID.randomUUID();
    private static final JobType jobType = JobType.EPUB_PUBLISH;
    private static final UUID notificationID = UUID.randomUUID();
    private static final NotificationType notificationType = NotificationType.EPUB_PUBLISH;
    private static final NotificationStatus notificationStatus = NotificationStatus.COMPLETED;
    private static final ArtifactType artifactType = ArtifactType.EPUB_PUBLISH;
    private static final PublicationOutputType publicationOutputType = PublicationOutputType.EPUB_ETEXT;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        PublicationSummary publicationSummary = new PublicationSummary()
                .setId(publicationID)
                .setOutputType(publicationOutputType);
        PublicationMetadata publicationMetadata = new PublicationMetadata()
                .setPublicationId(publicationID)
                .setCreatedBy(accountID)
                .setCreatedAt(UUIDs.timeBased());
        PublishedActivity publishedActivity = new PublishedActivity()
                .setPublicationId(publicationID)
                .setActivityId(activityID)
                .setStatus(ActivityPublicationStatus.ACTIVE)
                .setVersion(version);
        PublicationByExport publicationByExport = new PublicationByExport()
                .setPublicationId(publicationID)
                .setExportId(exportID);
        PublicationMetadataByPublishedActivity metadataByActivity = (PublicationMetadataByPublishedActivity) new PublicationMetadataByPublishedActivity()
                .setActivityId(activityID)
                .setVersion(version)
                .setCreatedBy(accountID);
        JobByPublication jobByPublication = new JobByPublication()
                .setPublicationId(publicationID)
                .setJobId(jobID);
        JobSummary jobSummary = new JobSummary()
                .setId(jobID)
                .setJobType(JobType.EPUB_PUBLISH)
                .setStatus(JobStatus.COMPLETED);
        AccountPayload accountPayload = new AccountPayload()
                .setAccountId(accountID);
        PublicationByActivity publicationByActivity = new PublicationByActivity()
                .setPublicationId(publicationID)
                .setActivityId(activityID);
        when(publicationGateway.fetchPublishedActivity(activityID)).thenReturn(Flux.just(publishedActivity));
        when(publicationGateway.fetchAllPublicationSummary()).thenReturn(Flux.just(publicationSummary));
        when(publicationGateway.fetchMetadataByPublication(publicationID)).thenReturn(Flux.just(publicationMetadata));
        when(publicationGateway.fetchPublishedActivityByPublication(publicationID)).thenReturn(Flux.just(publishedActivity));
        when(publicationGateway.persist(any(PublicationSummary.class))).thenReturn(Flux.just(new Void[]{}));
        when(publicationGateway.persist(any(PublishedActivity.class))).thenReturn(Flux.just(new Void[]{}));
        when(publicationGateway.persist(any(PublicationMetadataByPublishedActivity.class))).thenReturn(Flux.just(new Void[]{}));
        when(publicationGateway.persist(any(PublicationByExport.class))).thenReturn(Flux.just(new Void[]{}));
        when(publicationGateway.fetchPublicationByExport(exportID)).thenReturn(Flux.just(publicationByExport));
        when(publicationGateway.fetchMetadataByPublishedActivity(activityID)).thenReturn(Flux.just(metadataByActivity));
        when(publicationGateway.fetchMetadataByPublishedActivity(activityID, version)).thenReturn(Flux.just(metadataByActivity));
        when(accountService.getAccountPayload(any(UUID.class))).thenReturn(Mono.just(accountPayload));
        when(jobGateway.persist(any(Notification.class), any(UUID.class))).thenReturn(Flux.just(new Void[]{}));
        when(jobGateway.persist(any(JobSummary.class), any(UUID.class), any(ArtifactType.class))).thenReturn(Flux.just(new Void[]{}));
        when(jobGateway.persist(any(JobSummary.class), any(UUID.class))).thenReturn(Flux.just(new Void[]{}));
        when(jobGateway.fetchJobByPublication(publicationID)).thenReturn(Mono.just(jobByPublication));
        when(jobGateway.fetchJobSummary(jobID)).thenReturn(Mono.just(jobSummary));
        when(publicationGateway.persist(any(ActivityByAccount.class))).thenReturn(Flux.just(new Void[]{}));
        when(publicationGateway.fetchPublishedActivities()).thenReturn(Flux.just(publishedActivity));
        when(publicationGateway.fetchPublicationByActivity(activityID)).thenReturn(Flux.just(publicationByActivity));
        when(publicationGateway.fetchPublicationSummary(publicationID)).thenReturn(Flux.just(publicationSummary));
    }

    @Test
    void fetch_publications() {
        List<PublicationPayload> list = publicationService.fetchPublicationWithMeta().collectList().block();
        assertNotNull(list);
        assertEquals(1, list.size());
    }

    @Test
    void create_publication() {
        UUID publicationId = publicationService.createPublication(activityID,
                accountID,
                exportID,
                title,
                description,
                author,
                version,
                config,
                publicationOutputType).block();

        assertNotNull(publicationId);

        ArgumentCaptor<PublicationSummary> summaryCaptor = ArgumentCaptor.forClass(PublicationSummary.class);
        ArgumentCaptor<PublishedActivity> activityCaptor = ArgumentCaptor.forClass(PublishedActivity.class);
        ArgumentCaptor<PublicationMetadataByPublishedActivity> metadataCaptor = ArgumentCaptor.forClass(PublicationMetadataByPublishedActivity.class);
        ArgumentCaptor<PublicationByExport> publicationExportCaptor = ArgumentCaptor.forClass(PublicationByExport.class);
        ArgumentCaptor<ActivityByAccount> activityByAccountCaptor = ArgumentCaptor.forClass(ActivityByAccount.class);

        verify(publicationGateway).persist(summaryCaptor.capture());
        verify(publicationGateway).persist(activityCaptor.capture());
        verify(publicationGateway).persist(metadataCaptor.capture());
        verify(publicationGateway).persist(publicationExportCaptor.capture());
        verify(publicationGateway).persist(activityByAccountCaptor.capture());

        final PublicationSummary persistedSummary = summaryCaptor.getValue();

        assertAll(() -> {
            assertNotNull(persistedSummary);
            assertNotNull(persistedSummary.getId());
            // TODO test the config when passed along
            assertEquals("", persistedSummary.getConfig());
            assertEquals(description, persistedSummary.getDescription());
            assertEquals(title, persistedSummary.getTitle());
        });

        final PublishedActivity persistedPublishedActivity = activityCaptor.getValue();

        assertAll(() -> {
            assertNotNull(persistedPublishedActivity);
            assertEquals(activityID, persistedPublishedActivity.getActivityId());
            assertEquals(persistedSummary.getId(), persistedPublishedActivity.getPublicationId());
            assertEquals(description, persistedPublishedActivity.getDescription());
            assertEquals(title, persistedPublishedActivity.getTitle());
            assertEquals(version, persistedPublishedActivity.getVersion());
        });

        final PublicationMetadataByPublishedActivity persistedMetadata = metadataCaptor.getValue();

        assertAll(() -> {
            assertNotNull(persistedMetadata);
            assertEquals(activityID, persistedMetadata.getActivityId());
            assertNotNull(persistedMetadata.getCreatedAt());
            assertNotNull(persistedMetadata.getCreatedBy());
            assertEquals(persistedSummary.getId(), persistedMetadata.getPublicationId());
            assertEquals(version, persistedMetadata.getVersion());
            assertEquals(author, persistedMetadata.getAuthor());
            assertNull(persistedMetadata.getUpdatedBy());
            assertEquals(accountID, persistedMetadata.getCreatedBy());
            assertNotNull(persistedMetadata.getCreatedAt());
            assertNull(persistedMetadata.getUpdatedAt());
        });

        final PublicationByExport publicationByExport = publicationExportCaptor.getValue();

        assertAll(() -> {
            assertEquals(persistedSummary.getId(), publicationByExport.getPublicationId());
            assertEquals(exportID, publicationByExport.getExportId());
        });

        final ActivityByAccount activityByAccount = activityByAccountCaptor.getValue();

        assertAll(() -> {
            assertEquals(accountID, activityByAccount.getAccountId());
            assertEquals(activityID, activityByAccount.getActivityId());
        });
    }

    @Test
    void update_publication() {
        PublicationMetadata metadata = publicationService
                .updatePublicationMetadata(exportID, activityID, etextVersion, bookId).blockFirst();

        assertNotNull(metadata);

        ArgumentCaptor<PublicationMetadataByPublishedActivity> metadataCaptor = ArgumentCaptor.forClass(PublicationMetadataByPublishedActivity.class);

        verify(publicationGateway).persist(metadataCaptor.capture());

        PublicationMetadataByPublishedActivity metadataActivity = metadataCaptor.getValue();

        assertAll(() -> {
            assertEquals(activityID, metadataActivity.getActivityId());
            assertEquals(version, metadataActivity.getVersion());
            assertEquals(etextVersion, metadataActivity.getEtextVersion());
            assertEquals(bookId, metadataActivity.getBookId());
        });
    }

    @Test
    void save_etext_notification() {
        Notification notification = new Notification()
                .setId(notificationID)
                .setNotificationType(notificationType)
                .setNotificationStatus(notificationStatus);

        publicationService.saveEtextNotification(notification, jobID).blockFirst();
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        ArgumentCaptor<UUID> idCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(jobGateway).persist(notificationCaptor.capture(), idCaptor.capture());

        Notification notificationCap = notificationCaptor.getValue();
        UUID jobIDVerify = idCaptor.getValue();
        assertAll(() -> {
            assertEquals(jobID, jobIDVerify);
            assertEquals(notificationID, notificationCap.getId());
            assertEquals(notificationType, notificationCap.getNotificationType());
            assertEquals(notificationStatus, notificationCap.getNotificationStatus());
        });
    }

    @Test
    void save_job_artifact() {
        JobSummary jobSummary = new JobSummary()
                .setId(jobID)
                .setJobType(jobType);

        publicationService.saveJobAndArtifact(jobSummary, publicationID, artifactType).blockFirst();
        ArgumentCaptor<JobSummary> jobSummaryCaptor = ArgumentCaptor.forClass(JobSummary.class);
        ArgumentCaptor<UUID> idCaptor = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<ArtifactType> typeCaptor = ArgumentCaptor.forClass(ArtifactType.class);
        verify(jobGateway).persist(jobSummaryCaptor.capture(), idCaptor.capture(), typeCaptor.capture());

        JobSummary jobSummaryCap = jobSummaryCaptor.getValue();
        UUID idVerify = idCaptor.getValue();
        ArtifactType typeVerify = typeCaptor.getValue();
        assertAll(() -> {
            assertEquals(jobID, jobSummaryCap.getId());
            assertEquals(jobType, jobSummaryCap.getJobType());
            assertEquals(publicationID, idVerify);
            assertEquals(artifactType, typeVerify);
        });
    }

    @Test
    void save_job_publication() {
        JobSummary jobSummary = new JobSummary()
                .setId(jobID)
                .setJobType(jobType);

        publicationService.saveJobAndPublication(jobSummary, publicationID).blockFirst();
        ArgumentCaptor<JobSummary> jobSummaryCaptor = ArgumentCaptor.forClass(JobSummary.class);
        ArgumentCaptor<UUID> idCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(jobGateway).persist(jobSummaryCaptor.capture(), idCaptor.capture());

        JobSummary jobSummaryCap = jobSummaryCaptor.getValue();
        UUID idVerify = idCaptor.getValue();
        assertAll(() -> {
            assertEquals(jobID, jobSummaryCap.getId());
            assertEquals(jobType, jobSummaryCap.getJobType());
            assertEquals(publicationID, idVerify);
        });
    }

    @Test
    void fetch_publication_by_export() {
        PublicationByExport publicationByExport = publicationService.fetchPublicationByExport(exportID).block();
        assertNotNull(publicationByExport);
    }

    @Test
    void getOculusReviewStatus() {
        PublicationOculusData publicationOculusData = new PublicationOculusData();
        publicationOculusData.setOculusStatus("Published Live");
        publicationOculusData.setOculusVersion("v3");
        publicationOculusData.setBookId("BRNT-DF9FN6S37NB");
        when(publicationBroker.getOculusStatus(anyString())).thenReturn(Mono.just(publicationOculusData));
        PublicationOculusData publicationOculusResp = publicationService.getOculusStatus("BRNT-DF9FN6S37NB");
        assertNotNull(publicationOculusResp);
        assertEquals("Published Live", publicationOculusResp.getOculusStatus());
    }

    @Test
    void fetch_published_activities() {
        List<PublicationActivityPayload> list = publicationService.fetchPublishedActivities().collectList().block();
        assertNotNull(list);
        assertEquals(1, list.size());
    }

    @Test
    void fetch_publication_histories() {
        List<PublicationPayload> list = publicationService.fetchPublicationForActivity(activityID).collectList().block();
        assertNotNull(list);
        assertEquals(1, list.size());
    }

    @Test
    void fetch_publication_histories_for_output_type() {
        List<PublicationPayload> list = publicationService.fetchPublicationForActivity(activityID, publicationOutputType).collectList().block();
        assertNotNull(list);
        assertEquals(1, list.size());
    }

    @Test
    void fetch_publication_histories_for_output_type_inprogress() {
        Instant now = LocalDateTime.now().toDate().toInstant().minus(1, ChronoUnit.DAYS);
        JobSummary jobSummary = new JobSummary()
                .setId(jobID)
                .setJobType(JobType.EPUB_PUBLISH)
                .setStatus(JobStatus.STARTED);
        PublicationMetadata publicationMetadata = new PublicationMetadata()
                .setPublicationId(publicationID)
                .setCreatedBy(accountID)
                .setCreatedAt(UUIDs.startOf(now.toEpochMilli()));
        when(jobGateway.fetchJobSummary(jobID)).thenReturn(Mono.just(jobSummary));
        when(publicationGateway.fetchMetadataByPublication(publicationID)).thenReturn(Flux.just(publicationMetadata));
        List<PublicationPayload> list = publicationService.fetchPublicationForActivity(activityID, publicationOutputType).collectList().block();
        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals(Boolean.TRUE, list.stream().anyMatch(publicationPayload -> publicationPayload.getPublicationJobStatus().equals(JobStatus.FAILED)));
        assertEquals(Boolean.TRUE, list.stream().anyMatch(publicationPayload -> publicationPayload.getStatusMessage().equals("10003")));
    }

    @Test
    void delete_publication_history() {
        when(publicationGateway.updateActivityPublicationStatus(publicationID, ActivityPublicationStatus.DELETED, activityID, version)).thenReturn(Flux.just((new Void[]{})));
        publicationService.deletePublicationHistory(publicationID, activityID, version);
        verify(publicationGateway).updateActivityPublicationStatus(any(UUID.class), any(ActivityPublicationStatus.class), any(UUID.class), any(String.class));
    }

    @Test
    void updateTitle() {
        String titleToUpdate = "New title";
        String version = "version";

        when(publicationGateway.updateTitle(activityID, titleToUpdate, version)).thenReturn(Mono.empty());
        publicationService.updateTitle(activityID, titleToUpdate, version).block();
        verify(publicationGateway, atLeastOnce()).updateTitle(activityID, titleToUpdate, version);
    }
}
