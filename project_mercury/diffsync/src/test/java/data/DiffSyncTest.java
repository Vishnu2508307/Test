package data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import reactor.core.publisher.Flux;

public class DiffSyncTest {

    @InjectMocks
    DiffSync diffSync;
    @Mock
    Channel channel;
    @Mock
    ServerShadow serverShadow;
    @Mock
    ServerBackup serverBackup;
    @Mock
    DiffSyncProvider diffSyncProvider;
    @Mock
    DiffSyncIdentifier diffSyncIdentifier;
    @Mock
    DiffSyncEntity diffSyncEntity;
    @Mock
    DiffSyncGateway diffSyncGateway;

    DiffMatchPatchCustom dmp;

    ServerText serverText;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(serverShadow.getN()).thenReturn(new Version().setValue(Long.valueOf(0)));
        when(serverShadow.getContent()).thenReturn("cat");
        when(serverShadow.getDiffSyncIdentifier()).thenReturn(diffSyncIdentifier);

        when(serverBackup.getM()).thenReturn(new Version().setValue(Long.valueOf(0)));
        when(serverBackup.getContent()).thenReturn("cat");
        when(serverBackup.getDiffSyncIdentifier()).thenReturn(diffSyncIdentifier);
        serverText = new ServerText(diffSyncIdentifier, "cat");
        dmp = new DiffMatchPatchCustom();
    }

    @Test
    public void testHandlePatch_success() {
        when(serverShadow.diff(any(Patchable.class))).thenReturn(new Patch());
        when(serverShadow.getM()).thenReturn(new Version().setValue(Long.valueOf(0)));
        when(serverShadow.apply(any(Patch.class))).
                thenReturn(new ServerShadow(diffSyncIdentifier,
                                            "cats",
                                            new Version().setValue(Long.valueOf(1)),
                                            new Version().setValue(Long.valueOf(0))));
        when(diffSyncGateway.savePatch(any(PatchSummary.class))).thenReturn(Flux.empty());


        diffSync = new DiffSync(serverShadow, serverBackup, channel, diffSyncEntity, diffSyncIdentifier, diffSyncGateway);

        List<Patch> patches = new ArrayList<>();

        // first patch
        LinkedList<DiffMatchPatchCustom.Patch> diffMatchPatch_one;
        String text1 = "";
        String text2 = "s";
        LinkedList<DiffMatchPatchCustom.Diff> diffs_one = dmp.diffMain(text1, text2, false);
        diffMatchPatch_one = dmp.patchMake(diffs_one);

        Patch requestPatch_one = new Patch()
                .setClientId("client1")
                .setId(UUID.randomUUID())
                .setPatches(diffMatchPatch_one)
                .setM(new Version().setValue(Long.valueOf(0))).
                        setN(new Version().setValue(Long.valueOf(0)));

        patches.add(requestPatch_one);

        diffSync.handlePatch(patches, serverText);
        verify(serverShadow, times(1)).apply(requestPatch_one);

    }

    @Test
    public void testHandlePatch_rollback() {
        when(serverBackup.getM()).thenReturn(new Version().setValue(Long.valueOf(1)));
        when(serverShadow.getM()).thenReturn(new Version().setValue(Long.valueOf(2)));
        when(serverShadow.apply(any(Patch.class))).
                thenReturn(new ServerShadow(diffSyncIdentifier,
                                            "cats",
                                            new Version().setValue(Long.valueOf(2)),
                                            new Version().setValue(Long.valueOf(0))));


        diffSync = new DiffSync(serverShadow, serverBackup, channel, diffSyncEntity, diffSyncIdentifier, diffSyncGateway);

        List<Patch> patches = new ArrayList<>();

        // first patch
        LinkedList<DiffMatchPatchCustom.Patch> diffMatchPatch_one;
        String text1 = "";
        String text2 = "s";
        LinkedList<DiffMatchPatchCustom.Diff> diffs_one = dmp.diffMain(text1, text2, false);
        diffMatchPatch_one = dmp.patchMake(diffs_one);

        Patch requestPatch_one = new Patch()
                .setClientId("client1")
                .setId(UUID.randomUUID())
                .setPatches(diffMatchPatch_one)
                .setM(new Version().setValue(Long.valueOf(0))).
                        setN(new Version().setValue(Long.valueOf(0)));

        patches.add(requestPatch_one);

        DiffSyncRollbackFault ise = assertThrows(DiffSyncRollbackFault.class,
                                                 () -> diffSync.handlePatch(patches, serverText));

        assertEquals("cannot rollback this far back", ise.getMessage());

        verify(serverShadow, times(0)).apply(requestPatch_one);

    }
}
