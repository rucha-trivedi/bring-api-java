package com.bring.api.tracking.dao;

import com.bring.api.BringParser;
import com.bring.api.connection.BringConnection;
import com.bring.api.exceptions.RequestFailedException;
import com.bring.api.exceptions.UnmarshalException;
import com.bring.api.tracking.request.TrackingQuery;
import com.bring.api.tracking.request.Version;
import com.bring.api.tracking.response.TrackingResponse;
import com.bring.api.tracking.response.v1.Consignment;
import com.bring.api.tracking.response.v1.Event;
import com.bring.api.tracking.response.v1.Package;
import com.bring.api.tracking.response.v1.Signature;
import com.bring.api.tracking.response.v1.TrackingResult;
import com.bring.api.tracking.response.v2.TrackingResultV2;
import no.bring.sporing._2.ConsignmentSet;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class TrackingDao {

    final String OPEN_TRACKING_BASE_URL = "http://sporing.bring.no/api/{version}/tracking.xml";
    final String LOGGED_IN_TRACKING_BASE_URL = "https://www.mybring.com/tracking/api/{version}/tracking.xml";

    private BringConnection bringConnection;
    private BringParser<TrackingResult> bringParserV1;
    private BringParser<ConsignmentSet> bringParserV2;

    public TrackingDao(BringConnection connection){
        bringParserV1 = new BringParser<TrackingResult>(TrackingResult.class);
        bringParserV2 = new BringParser<ConsignmentSet>(ConsignmentSet.class);
        this.bringConnection = connection;
    }

    public TrackingDao(BringConnection bringConnection, BringParser<TrackingResult> bringParserV1) {
        this.bringConnection = bringConnection;
        this.bringParserV1 = bringParserV1;
    }

    public TrackingResult query(TrackingQuery trackingQuery) throws RequestFailedException {
        if(trackingQuery.getVersion() != Version.v1) {
            throw new RequestFailedException("Version not supported", 400);
        }
        return (TrackingResult) queryWithVersion(trackingQuery);
    }

    public TrackingResponse queryWithVersion(TrackingQuery trackingQuery) throws RequestFailedException {
        String baseUrl = getOpenTrackingBaseUrl(trackingQuery);
        if(trackingQuery.hasOptionalUrl()){
            baseUrl = trackingQuery.getOptionalUrl();
        }

        return query(baseUrl, trackingQuery, null);
    }

    public TrackingResult query(TrackingQuery trackingQuery, String apiUserId, String apiKey) throws RequestFailedException {
        Map<String,String> headers = new HashMap<String,String>();
        headers.put("X-MyBring-API-Uid", apiUserId);
        headers.put("X-MyBring-API-Key", apiKey);

        String baseUrl = getLoggedInTrackingBaseUrl(trackingQuery);
        if(trackingQuery.hasOptionalUrl()){
            baseUrl = trackingQuery.getOptionalUrl();
        }
        return (TrackingResult) query(baseUrl, trackingQuery, headers);
    }
    
    private TrackingResponse query(String baseUrl, TrackingQuery trackingQuery, Map<String, String> headers) throws RequestFailedException {
        String url = baseUrl + trackingQuery.toQueryString();
        InputStream inputStream = null;
        try {
            if (headers == null) {
                inputStream = bringConnection.openInputStream(url);
            }
            else {
                inputStream = bringConnection.openInputStream(url, headers);
            }
            TrackingResponse trackingResult = getTrackingResponse(trackingQuery, inputStream, baseUrl);
            //convertSignatureUrlsToFullUrl(trackingResult, baseUrl.substring(0, baseUrl.lastIndexOf('/') + 1));
            return trackingResult;
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

    private TrackingResponse getTrackingResponse(TrackingQuery trackingQuery, InputStream inputStream, String baseUrl) throws UnmarshalException {
        switch (trackingQuery.getVersion()) {
            case v1 :
                return getV1Response(inputStream, baseUrl);
            case v2 :
                return getV2Response(inputStream);
            default :
                throw new RuntimeException("not supported tracking request version.");
        }
    }

    private TrackingResponse getV1Response(InputStream inputStream, String baseUrl) throws UnmarshalException {
        TrackingResult trackingResult = bringParserV1.unmarshal(inputStream);
        convertSignatureUrlsToFullUrl(trackingResult, baseUrl);
        return trackingResult;
    }

    private TrackingResponse getV2Response(InputStream inputStream) throws UnmarshalException {
        ConsignmentSet consignmentSetType = bringParserV2.unmarshal(inputStream);
        return new TrackingResultV2(consignmentSetType);
    }

    private void convertSignatureUrlsToFullUrl(TrackingResult trackingResult, String baseUrl) {
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

    private String getOpenTrackingBaseUrl(TrackingQuery trackingQuery) {
        return OPEN_TRACKING_BASE_URL.replace("{version}", trackingQuery.getVersion().name());
    }

    private String getLoggedInTrackingBaseUrl(TrackingQuery trackingQuery) {
        return LOGGED_IN_TRACKING_BASE_URL.replace("{version}", trackingQuery.getVersion().name());
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