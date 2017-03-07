package com.bring.api.tracking.dao;

import com.bring.api.connection.HttpUrlConnectionAdapter;
import com.bring.api.exceptions.RequestFailedException;
import com.bring.api.tracking.request.TrackingQuery;
import com.bring.api.tracking.request.Version;
import com.bring.api.tracking.response.TrackingResponse;
import com.bring.api.tracking.response.v2.TrackingResult;
import no.bring.sporing._2.ConsigmentElementType;
import no.bring.sporing._2.ConsignmentSet;
import no.bring.sporing._2.PackageCargoConsignmentType;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class TrackingDaoIntegrationTest {
    TrackingDao dao;
    com.bring.api.tracking.response.v1.TrackingResult cst;
        
    @Before
    public void setUp() throws RequestFailedException {
        dao = new TrackingDao(new HttpUrlConnectionAdapter("test"));
        TrackingQuery query = new TrackingQuery();
        query.withQueryNumber("TESTPACKAGELOADEDFORDELIVERY");
        cst = dao.query(query);
    }
    
    @Test
    public void shouldFindConsignmentSetOnValidSearch() throws RequestFailedException {
        assertEquals(com.bring.api.tracking.response.v1.TrackingResult.class, cst.getClass());
    }
    
    @Test
    public void shouldFindTotalWeigthOnValidSearch() throws RequestFailedException {
        String totalWeight = cst.getConsignments().get(0).getTotalWeight().getValue();
        assertEquals("16.5", totalWeight);
    }
    
    @Test(expected = RequestFailedException.class)
    public void shouldThrowUnmarshalExceptionOnSearchWithWrongParameter() throws RequestFailedException {
        TrackingQuery query = new TrackingQuery();
        query.withQueryNumber("70438101015432113xc");
        cst = dao.query(query);
    }
    
    @Test(expected = RequestFailedException.class)
    public void shouldHandleInvalidLogins() throws RequestFailedException {
        TrackingDao trackingDao = new TrackingDao(new HttpUrlConnectionAdapter("test"));
        trackingDao.query(new TrackingQuery("1234567"), "username", "apiKey");
    }

    @Test
    public void should_parse_v2_response_from_tracking() throws Exception {
        dao = new TrackingDao(new HttpUrlConnectionAdapter("test"));
        TrackingQuery query = new TrackingQuery();
        query.withQueryNumber("TESTPACKAGELOADEDFORDELIVERY");
        query.withOptionalVersion(Version.v2);
        TrackingResponse trackingResponse = dao.queryWithVersion(query);

        ConsignmentSet consignmentSet = ((TrackingResult) trackingResponse).getConsignmentSet();
        List<ConsigmentElementType> consignments = consignmentSet.getConsignment();
        String consignmentId = ((PackageCargoConsignmentType) consignments.get(0)).getConsignmentId();


        assertEquals("2", consignmentSet.getApiVersion());
        assertEquals("SHIPMENTNUMBER", consignmentId);
    }
}
