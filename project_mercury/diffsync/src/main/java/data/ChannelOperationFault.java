package data;

import com.smartsparrow.exception.Fault;

public class ChannelOperationFault extends Fault {

    private static final long serialVersionUID = -3656581513723680811L;

    public ChannelOperationFault(String message) {
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
