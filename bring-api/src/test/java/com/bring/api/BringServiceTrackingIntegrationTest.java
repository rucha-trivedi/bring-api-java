package com.bring.api;

import com.bring.api.exceptions.RequestFailedException;
import com.bring.api.tracking.request.TrackingQuery;
import com.bring.api.tracking.request.Version;
import com.bring.api.tracking.response.v2.TrackingResult;
import no.bring.sporing._2.ConsigmentElementType;
import no.bring.sporing._2.EventType;
import no.bring.sporing._2.PackageCargoConsignmentType;
import no.bring.sporing._2.PackageCargoEventType;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class BringServiceTrackingIntegrationTest {
    BringService service;
    
    @Before
    public void setUp(){
        service = new BringService("test");
    }
    
    @Test
    public void shouldBeAbleTofindPackage() throws RequestFailedException {
        TrackingQuery query = new TrackingQuery();
        query.withQueryNumber("TESTPACKAGEATPICKUPPOINT");
        com.bring.api.tracking.response.v1.TrackingResult result = service.queryTracking(query);
        assertEquals(com.bring.api.tracking.response.v1.TrackingResult.class, result.getClass());
    }
    
    @Test
    public void shouldBeAbleToFindLatestStatus() throws RequestFailedException {
        TrackingQuery query = new TrackingQuery("TESTPACKAGEDELIVERED");
        String result = service.queryTracking(query).getConsignment(0).getPackage(0).getEvent(0).getDescription();
        assertEquals("Sendingen er utlevert.", result);
    }

    @Test(expected = RequestFailedException.class)
    public void shouldBeAbleToFindThrowBadRequestForOldMethodUsed() throws RequestFailedException {
        TrackingQuery query = new TrackingQuery("TESTPACKAGEDELIVERED");
        query.withOptionalVersion(Version.v2);
        service.queryTracking(query);
    }

    @Test
    public void shouldBeAbleToFindLatestStatusForVersion() throws RequestFailedException {
        TrackingQuery query = new TrackingQuery("TESTPACKAGEDELIVERED");
        query.withOptionalVersion(Version.v2);
        List<ConsigmentElementType> consignment = ((TrackingResult) service.queryTrackingWithVersion(query)).getConsignmentSet().getConsignment();
        EventType eventType = ((PackageCargoConsignmentType) consignment.get(0)).getPackageSet().getPackage().get(0).getEventSet().getEvent().get(0);
        assertEquals("Sendingen er utlevert.", ((PackageCargoEventType) eventType).getDescription());
    }
}
