package com.smartsparrow.asset.lang;

import java.util.List;

public class AssetURNParseException extends RuntimeException {

    private static final String ERROR_MESSAGE = "URN '%s' can not be parsed";

    public AssetURNParseException(String urn) {
        super(String.format(ERROR_MESSAGE, urn));
    }

    public AssetURNParseException(List<String> urns) {
        super(String.format(ERROR_MESSAGE, urns.toString()));
    }
    public AssetURNParseException(String urn, Throwable cause) {
        super(String.format(ERROR_MESSAGE, urn), cause);
    }

}
