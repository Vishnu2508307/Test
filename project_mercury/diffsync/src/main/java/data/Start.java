package data;

import java.io.Serializable;

public class Start implements Exchangeable, Serializable {

    private static final long serialVersionUID = 2997179687824124010L;
    @Override
    public Type getType() {
        return Type.START;
    }
}
