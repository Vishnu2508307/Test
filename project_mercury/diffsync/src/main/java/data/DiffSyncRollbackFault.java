package data;

import com.smartsparrow.exception.IllegalArgumentFault;

public class DiffSyncRollbackFault extends IllegalArgumentFault {

    /**
     * Construct a fault when diff sync rollback fails
     *
     * @param message
     */
    public DiffSyncRollbackFault(final String message) {
        super(message);
    }
}
