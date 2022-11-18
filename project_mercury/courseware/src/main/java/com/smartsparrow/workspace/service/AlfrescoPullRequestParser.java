package com.smartsparrow.workspace.service;

import static com.smartsparrow.asset.data.AlfrescoUpdateResponse.ALT_TEXT_FIELD;
import static com.smartsparrow.asset.data.AlfrescoUpdateResponse.ENTRY_FIELD;
import static com.smartsparrow.asset.data.AlfrescoUpdateResponse.HEIGHT_FIELD;
import static com.smartsparrow.asset.data.AlfrescoUpdateResponse.LONG_DESC_FIELD;
import static com.smartsparrow.asset.data.AlfrescoUpdateResponse.VERSION_FIELD;
import static com.smartsparrow.asset.data.AlfrescoUpdateResponse.WIDTH_FIELD;
import static com.smartsparrow.asset.data.AlfrescoUpdateResponse.WORK_URN_FIELD;
import static com.smartsparrow.util.Warrants.affirmArgument;
import static com.smartsparrow.data.Headers.BRONTE_ASSET_HEADER;
import static com.smartsparrow.data.Headers.ALFRESCO_AZURE_AUTHORIZATION_HEADER;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.httpclient.HttpStatus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartsparrow.asset.data.AlfrescoImageNode;
import com.smartsparrow.asset.data.AlfrescoNode;
import com.smartsparrow.asset.data.AlfrescoUpdateResponse;
import com.smartsparrow.asset.eventmessage.AlfrescoAssetPullEventMessage;
import com.smartsparrow.asset.service.AlfrescoAssetService;
import com.smartsparrow.config.AssetConfig;
import com.smartsparrow.data.MimeType;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.ext_http.service.HttpEvent;
import com.smartsparrow.ext_http.service.ResultNotification;
import com.smartsparrow.util.Json;
import com.smartsparrow.workspace.cms.AlfrescoAssetPullRequestMessage;

public class AlfrescoPullRequestParser {

    private static final ObjectMapper om = new ObjectMapper();

    private final AssetConfig assetConfig;
    private final AlfrescoAssetService alfrescoAssetService;

    @Inject
    public AlfrescoPullRequestParser(AssetConfig assetConfig, AlfrescoAssetService alfrescoAssetService) {
        this.assetConfig = assetConfig;
        this.alfrescoAssetService = alfrescoAssetService;
    }

    /**
     * Extracts a series of fields from the result notification object to build an {@link AlfrescoAssetPullEventMessage}
     *
     * @param resultNotification the result notification to build the event message from
     * @return the built alfresco asset pull event message
     * @throws JsonProcessingException when failing to parse
     * @throws IllegalArgumentFault when any of the required field is missing from the resultNotification
     */
    public AlfrescoAssetPullEventMessage parse(final ResultNotification resultNotification) throws JsonProcessingException {
        affirmArgument(resultNotification != null, "resultNotification is required");
        affirmArgument(resultNotification.getResult() != null, "results are required");
        affirmArgument(!resultNotification.getResult().isEmpty(), "results are required");

        // get all the events from the notification
        List<HttpEvent> events = resultNotification.getResult();

        // find the request event
        final HttpEvent requestEvent = events.stream()
                .filter(event -> event.getOperation().equals(HttpEvent.Operation.request))
                .findFirst()
                .orElse(null);

        // find the response event
        final HttpEvent responseEvent = events.stream()
                .filter(event -> event.getOperation().equals(HttpEvent.Operation.response))
                .findFirst()
                .orElse(null);

        // throw an error when events does not contain neither the request nor the response events
        affirmArgument(requestEvent != null, "requestEvent missing");
        affirmArgument(responseEvent != null, "responseEvent missing");

        // we are expecting a 200 ok response here
        // throw an error if the response did not complete with an ok
        affirmArgument(responseEvent.getStatusCode().equals(HttpStatus.SC_OK), "response had a non 200 status");

        // begin parsing
        final Map<String, List<String>> requestHeaders = requestEvent.getHeaders();

        // check that the header exists
        affirmArgument(requestHeaders.containsKey(BRONTE_ASSET_HEADER),
                String.format("missing required %s header", BRONTE_ASSET_HEADER));

        List<String> bronteAssetHeaders = requestHeaders.get(BRONTE_ASSET_HEADER);
        List<String> ssoHeaders = requestHeaders.get(ALFRESCO_AZURE_AUTHORIZATION_HEADER);

        // check that the required headers exist and are valid
        affirmArgument(bronteAssetHeaders != null, String.format("missing required %s header", BRONTE_ASSET_HEADER));
        affirmArgument(!bronteAssetHeaders.isEmpty(), String.format(" required %s header is empty", BRONTE_ASSET_HEADER));
        affirmArgument(ssoHeaders != null, String.format("missing required %s header", ALFRESCO_AZURE_AUTHORIZATION_HEADER));
        affirmArgument(!ssoHeaders.isEmpty(), String.format(" required %s header is empty", ALFRESCO_AZURE_AUTHORIZATION_HEADER));

        // we got the message!
        final AlfrescoAssetPullRequestMessage message = om.readValue(bronteAssetHeaders.get(0), AlfrescoAssetPullRequestMessage.class);

        String eventBody = escapeSequenceRemove(responseEvent.getBody());

        eventBody = eventBody.substring(1, eventBody.length() - 1);

        // now we need to read the metadata and the inputStream from the response
        final Map<String, String> bodyMap = Json.toMap(eventBody);

        // ensure the response has an entry field, that's where all the info is
        affirmArgument(bodyMap.containsKey(ENTRY_FIELD), "invalid response body");

        // parse the alfresco update response
        final AlfrescoUpdateResponse response = om.readValue(bodyMap.get(ENTRY_FIELD), AlfrescoUpdateResponse.class);

        affirmArgument(response.getPath() != null, "missing required path info");

        // get path name from the response
        String pathName = response.getPath().get("name").toString();
        String alfrescoPath = alfrescoAssetService.getAlfrescoPath(pathName);

        // read and convert the lastModifiedAt field from the alfresco response
        String lastModifiedString = response.getModifiedAt() != null ? response.getModifiedAt() : response.getCreatedAt();
        long lastModifiedAt = LocalDateTime.parse(lastModifiedString.substring(0, lastModifiedString.indexOf(".")))
                .toEpochSecond(ZoneOffset.UTC);

        affirmArgument(response.getMimeType()!=null && response.getMimeType().split("/").length==2, "required MimeType is missing");
        // build the image node with the found information
        AlfrescoImageNode modifiedNode = new AlfrescoImageNode()
                .setMimeType(response.getMimeType())
                .setWidth(Double.valueOf(response.getProperty(WIDTH_FIELD, Integer.class))) //
                .setHeight(Double.valueOf(response.getProperty(HEIGHT_FIELD, Integer.class))) //
                .setSource(null) // FIXME is this a useful field to have ?
                .setAltText(response.getOrDefaultProperty(ALT_TEXT_FIELD, String.class, "")) //
                .setLongDescription(response.getOrDefaultProperty(LONG_DESC_FIELD, String.class, "")) //
                .setAlfrescoId(message.getAlfrescoNodeId())
                .setName(response.getName())
                .setVersion(response.getProperty(VERSION_FIELD, String.class)) //
                .setLastModifiedDate(lastModifiedAt)
                .setPath(alfrescoPath)
                .setWorkURN(response.getOrDefaultProperty(WORK_URN_FIELD, String.class, ""));

        // only read altText and longDesc to the metadata for now
        final Map<String, String> metadata = new HashMap<String, String>() {
            {put("altText", modifiedNode.getAltText());}
            {put("longDesc", modifiedNode.getLongDescription());}
            {put("alfrescoPath", modifiedNode.getPath());}
            {put("workURN", modifiedNode.getWorkURN());}
        };

        // compare the last modified with the new modified

        // build the pull event message
        return new AlfrescoAssetPullEventMessage()
                .setInputStream(null) // this is null at first
                .setAlfrescoNode(modifiedNode) //
                .setAlfrescoUrl(String.format("%s%s%s/content", assetConfig.getAlfrescoUrl(), "/alfresco/api/-default-/public/alfresco/versions/1/nodes/", modifiedNode.getAlfrescoId())) //
                .setMetadata(metadata) //
                .setRequireUpdate(requireUpdate(lastModifiedAt, message.getLastModified(),message.getVersion(), response.getProperty(VERSION_FIELD, String.class) )) //
                .setAssetId(message.getAssetId()) //
                .setMyCloudToken(ssoHeaders.get(0)) //
                .setReferenceId(message.getReferenceId()) //
                .setForceSync(message.isForceSync());
    }

    /**
     * Returns file name with extension. Needed for AssetUpload service.
     *
     * @param alfrescoNode the alfrescoNode to process
     * @return fileNameWithExtension to ensure file name has extension.
     */
    public  String getFileNameWithExtension(AlfrescoNode alfrescoNode){
        final String fileNameWithExtension = alfrescoNode.getName().replaceAll("\\s", "_");
        affirmArgument(alfrescoNode instanceof AlfrescoImageNode,"Alfresco node is not image type");
        final MimeType fileMimeType = new MimeType(((AlfrescoImageNode)alfrescoNode).getMimeType());
        if (!fileNameWithExtension.contains("." + fileMimeType.getSubType())) {
            return fileNameWithExtension.concat("." + fileMimeType.getSubType());
        }
        return fileNameWithExtension;
    }

    /**
     * Turns the long into dates and compares them
     *
     * @param responseLastModified the lastModifiedAt from the alfresco response
     * @param messageLastModified the lastModifiedAt coming from Bronte
     * @return true if the response date is greater than the message date, false for any other case
     */
    private boolean requireUpdate(long responseLastModified, long messageLastModified, String bronteVersion, String alfrescoVersion) {
        return ((responseLastModified > messageLastModified) || (bronteVersion == null || !bronteVersion.equals(alfrescoVersion)));
    }

    /**
     * Remove escape sequences in Alfresco response
     *
     * @param responseBody the response body from Alfresco
     * @return response body without escape sequences
     */
    private String escapeSequenceRemove(String responseBody) {
        return responseBody.replace("\\\"", "\"")
                .replace("\\\\\"", "\\\"")
                .replace("\\\\", "\\")
                .replace("\\n", " ")
                .replace("\\r", "")
                .replace("\\t", "")
                .replace("\\v", "");
    }
}
