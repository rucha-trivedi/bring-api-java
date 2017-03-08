package com.bring.api.tracking.dao;

import com.bring.api.BringParser;
import com.bring.api.connection.BringConnection;
import com.bring.api.exceptions.RequestFailedException;
import com.bring.api.exceptions.UnmarshalException;
import com.bring.api.tracking.request.TrackingQuery;
import com.bring.api.tracking.response.AbstractTrackingResponse;
import com.bring.api.tracking.response.v1.Consignment;
import com.bring.api.tracking.response.v1.Event;
import com.bring.api.tracking.response.v1.Package;
import com.bring.api.tracking.response.v1.Signature;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static com.bring.api.tracking.request.Version.v1;
import static java.util.Objects.nonNull;

public class TrackingDao {

    final String OPEN_TRACKING_BASE_URL = "http://sporing.bring.no/api/{version}/tracking.xml";
    final String LOGGED_IN_TRACKING_BASE_URL = "https://www.mybring.com/tracking/api/{version}/tracking.xml";

    private BringConnection bringConnection;
    private BringParser<com.bring.api.tracking.response.v1.TrackingResult> bringParserV1;

    public TrackingDao(BringConnection connection){
        this.bringConnection = connection;
    }

    public TrackingDao(BringConnection bringConnection, BringParser<com.bring.api.tracking.response.v1.TrackingResult> bringParserV1) {
        this.bringConnection = bringConnection;
        this.bringParserV1 = bringParserV1;
    }

    @Deprecated
    public com.bring.api.tracking.response.v1.TrackingResult query(TrackingQuery trackingQuery) throws RequestFailedException {
        validateRequestIsForVersion1(trackingQuery);
        return (com.bring.api.tracking.response.v1.TrackingResult) queryWithVersion(trackingQuery);
    }

    public AbstractTrackingResponse queryWithVersion(TrackingQuery trackingQuery) throws RequestFailedException {
        String baseUrl = getOpenTrackingBaseUrl(trackingQuery);
        if(trackingQuery.hasOptionalUrl()){
            baseUrl = trackingQuery.getOptionalUrl();
        }

        return query(baseUrl, trackingQuery, null);
    }

    public com.bring.api.tracking.response.v1.TrackingResult query(TrackingQuery trackingQuery, String apiUserId, String apiKey) throws RequestFailedException {
        validateRequestIsForVersion1(trackingQuery);

        Map<String,String> headers = new HashMap<String,String>();
        headers.put("X-MyBring-API-Uid", apiUserId);
        headers.put("X-MyBring-API-Key", apiKey);

        String baseUrl = getLoggedInTrackingBaseUrl(trackingQuery);
        if(trackingQuery.hasOptionalUrl()){
            baseUrl = trackingQuery.getOptionalUrl();
        }
        return (com.bring.api.tracking.response.v1.TrackingResult) query(baseUrl, trackingQuery, headers);
    }
    
    private AbstractTrackingResponse query(String baseUrl, TrackingQuery trackingQuery, Map<String, String> headers) throws RequestFailedException {
        String url = baseUrl + trackingQuery.toQueryString();
        InputStream inputStream = null;
        try {
            if (headers == null) {
                inputStream = bringConnection.openInputStream(url);
            }
            else {
                inputStream = bringConnection.openInputStream(url, headers);
            }
            AbstractTrackingResponse trackingResponse = getTrackingResponse(trackingQuery, inputStream, baseUrl);
            return trackingResponse;
        }
        catch (UnmarshalException e) {
            throw new RequestFailedException(e);
        }
        finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                }
                catch (IOException e) {
                    throw new RequestFailedException(e);
                }
            }
        }
    }

    private AbstractTrackingResponse getTrackingResponse(TrackingQuery trackingQuery, InputStream inputStream, String baseUrl) throws UnmarshalException, RequestFailedException {
        AbstractTrackingResponse trackingResponse;

        if(trackingQuery.getVersion().is(v1) && nonNull(bringParserV1)) {
            trackingResponse = bringParserV1.unmarshal(inputStream);
        }
        else {
            trackingResponse = trackingQuery.getVersion().unmarshal(inputStream);
        }

        if(trackingQuery.getVersion().is(v1)) {
            convertSignatureUrlsToFullUrl((com.bring.api.tracking.response.v1.TrackingResult)trackingResponse, baseUrl);
        }

        return trackingResponse;
    }

    private void convertSignatureUrlsToFullUrl(com.bring.api.tracking.response.v1.TrackingResult trackingResult, String baseUrl) {
        String urlPrefix = baseUrl.substring(0, baseUrl.lastIndexOf('/') + 1);

        for (Consignment consignment : trackingResult.getConsignments()) {
            if (consignment.getPackageSet() != null && consignment.getPackageSet().getPackages() != null) {
                for (Package packet : consignment.getPackageSet().getPackages()) {
                    for (Event event : packet.getEventSet().getEvents()) {
                        convertToFullUrl(event, urlPrefix);
                    }
                }
            }
        }
    }

    private void convertToFullUrl(Event event, String urlPrefix) {
        String url = event.getSignature().getLinkToImage();
        if (url != null && !url.matches("^https?://.*")) {
            event.getSignature().setLinkToImage(urlPrefix + url);
        }
    }

    private void validateRequestIsForVersion1(TrackingQuery trackingQuery) throws RequestFailedException {
        if(!trackingQuery.getVersion().is(v1)) {
            throw new RequestFailedException("Version not supported : " + trackingQuery.getVersion(), 400);
        }
    }

    private String getOpenTrackingBaseUrl(TrackingQuery trackingQuery) {
        return OPEN_TRACKING_BASE_URL.replace("{version}", trackingQuery.getVersion().getValue());
    }

    private String getLoggedInTrackingBaseUrl(TrackingQuery trackingQuery) {
        return LOGGED_IN_TRACKING_BASE_URL.replace("{version}", trackingQuery.getVersion().getValue());
    }

    /**
     * Get the signature image.
     * Caller is responsible for closing stream.
     * 
     * @param signature
     * @return
     * @throws com.bring.api.exceptions.RequestFailedException
     */
	public InputStream getSignatureImageAsStream(Signature signature, String apiUserId, String apiKey) throws RequestFailedException {
        Map<String,String> headers = new HashMap<String,String>();
        headers.put("X-MyBring-API-Uid", apiUserId);
        headers.put("X-MyBring-API-Key", apiKey);
		return bringConnection.openInputStream(signature.getLinkToImage(), headers);
	}
	
	public InputStream getSignatureImageAsStream(Signature signature) throws RequestFailedException {
		return bringConnection.openInputStream(signature.getLinkToImage());
	}
}