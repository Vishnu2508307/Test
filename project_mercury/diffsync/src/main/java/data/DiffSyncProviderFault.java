package data;

import com.smartsparrow.exception.Fault;

public class DiffSyncProviderFault extends Fault {

    private static final long serialVersionUID = -4656581513723680811L;

    public DiffSyncProviderFault(String message) {
        super(message);
    }

    @Override
    public int getResponseStatusCode() {
        return 422;
    }

    @Override
    public String getType() {
        return "UNPROCESSABLE_ENTITY";
    }
}
