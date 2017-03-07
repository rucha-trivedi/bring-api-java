package com.bring.api.tracking.dao;

import com.bring.api.BringParser;
import com.bring.api.connection.BringConnection;
import com.bring.api.connection.HttpUrlConnectionAdapter;
import com.bring.api.exceptions.RequestFailedException;
import com.bring.api.exceptions.UnmarshalException;
import com.bring.api.tracking.request.TrackingQuery;
import com.bring.api.tracking.request.Version;
import com.bring.api.tracking.response.v1.Consignment;
import com.bring.api.tracking.response.v1.Signature;
import com.bring.api.tracking.response.v1.TrackingResult;
import no.bring.sporing._2.ConsigmentElementType;
import no.bring.sporing._2.ConsignmentSet;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class TrackingDaoTest {
    private BringConnection bringConnectionMock;
    private BringConnection bringConnectionMock2;
    private final HttpURLConnection connectionMock = mock(HttpURLConnection.class);
    private TrackingResult trackingResultMock;
    private ConsignmentSet consignmentSetMock;
    BringParser<TrackingResult> bringParserMock;
    BringParser<ConsignmentSet> bringParserMock2;
    TrackingDao dao;

    @Before
    public void setUp() throws IOException{
        bringConnectionMock2 = mock(BringConnection.class);
        bringConnectionMock = new HttpUrlConnectionAdapter("test") {
            @Override
            protected HttpURLConnection openConnection(URL url) throws IOException {
                return connectionMock;
            }
        };
        when(connectionMock.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);
        
        trackingResultMock = mock(TrackingResult.class);
        consignmentSetMock = mock(ConsignmentSet.class);
        when(trackingResultMock.getConsignments()).thenReturn(new ArrayList<Consignment>());
        when(consignmentSetMock.getConsignment()).thenReturn(new ArrayList<ConsigmentElementType>());

        bringParserMock = mock(BringParser.class);
        bringParserMock2 = mock(BringParser.class);
        when(bringParserMock.unmarshal((InputStream) any())).thenReturn(trackingResultMock);
        when(bringParserMock2.unmarshal((InputStream) any())).thenReturn(consignmentSetMock);

        dao = new TrackingDao(bringConnectionMock, bringParserMock);
    }

    @Test
    public void shouldNotSetLoginHeadersOnNormalFind() throws RequestFailedException, UnmarshalException {
        dao.query(new TrackingQuery("123456"));
        verify(connectionMock, never()).addRequestProperty("X-MyBring-API-Uid",eq(anyString()));
        verify(connectionMock, never()).addRequestProperty("X-MyBring-API-Key",eq(anyString()));
    }
    
    @Test
    public void shouldBeAbleToSetCustomHeaders() throws RequestFailedException, UnmarshalException {
        dao.query(new TrackingQuery("123456"), "username", "apiKey");
        verify(connectionMock).addRequestProperty("X-MyBring-API-Uid","username");
        verify(connectionMock).addRequestProperty("X-MyBring-API-Key","apiKey");
    }
    
    @Test
    public void shouldBeAbleToDownloadSignatureImage() throws RequestFailedException, IOException {
        TrackingDao trackingDao = new TrackingDao(bringConnectionMock);
        Signature signatureMock = mock(Signature.class);
        URL signatureUrl = getClass().getResource("signature.png");
        when(bringConnectionMock.openInputStream(signatureUrl.toString())).thenReturn(signatureUrl.openStream());
        when(signatureMock.getLinkToImage()).thenReturn(signatureUrl.toString());
        InputStream signatureStream = trackingDao.getSignatureImageAsStream(signatureMock);
        assertNotNull(signatureStream);
        assertTrue(signatureStream instanceof InputStream);
    }
    
    @Test(expected = RequestFailedException.class)
    public void shouldThrowFailedRequestIfSignatureDoesNotExist() throws RequestFailedException, IOException {
        TrackingDao trackingDao = new TrackingDao(bringConnectionMock2);
        Signature signatureMock = mock(Signature.class);
        URL signatureUrl = new URL("file://BringSignatureMockImageThatDoesNotExist.png");
        when(bringConnectionMock2.openInputStream(signatureUrl.toString())).thenThrow(new RequestFailedException());
//      Mockito.doThrow(new RequestFailedException()).when(bringConnectionMock).openInputStream(signatureUrl.toString());
        when(signatureMock.getLinkToImage()).thenReturn(signatureUrl.toString());
        InputStream signatureStream = trackingDao.getSignatureImageAsStream(signatureMock);
    }

    @Test
    public void should_use_standard_url_mybring() throws RequestFailedException, IOException {
        Map<String,String> headers = new HashMap<String,String>();
        headers.put("X-MyBring-API-Uid", "username");
        headers.put("X-MyBring-API-Key", "apiKey");

        dao = new TrackingDao(bringConnectionMock2, bringParserMock);
        dao.query(new TrackingQuery("123456"), "username", "apiKey");

        when(bringConnectionMock2.openInputStream(anyString())).thenReturn(null);
        verify(bringConnectionMock2).openInputStream("https://www.mybring.com/tracking/api/v1/tracking.xml?q=123456",
               headers);
    }

    @Test
    public void should_use_optional_url_mybring() throws RequestFailedException, IOException {
        Map<String,String> headers = new HashMap<String,String>();
        headers.put("X-MyBring-API-Uid", "username");
        headers.put("X-MyBring-API-Key", "apiKey");

        dao = new TrackingDao(bringConnectionMock2, bringParserMock);
        dao.query(new TrackingQuery("123456").withOptionalUrl("http://optional"), "username", "apiKey");

        when(bringConnectionMock2.openInputStream(anyString())).thenReturn(null);
        verify(bringConnectionMock2).openInputStream("http://optional?q=123456",
               headers);
    }

    @Test
    public void should_use_standard_url() throws RequestFailedException, IOException {
        dao = new TrackingDao(bringConnectionMock2, bringParserMock);
        dao.query(new TrackingQuery("123456"));

        when(bringConnectionMock2.openInputStream(anyString())).thenReturn(null);
        verify(bringConnectionMock2).openInputStream("http://sporing.bring.no/api/v1/tracking.xml?q=123456");
    }

    @Test
    public void should_use_optional_url() throws RequestFailedException, IOException {
        dao = new TrackingDao(bringConnectionMock2, bringParserMock);
        dao.query(new TrackingQuery("123456").withOptionalUrl("http://optional"));

        when(bringConnectionMock2.openInputStream(anyString())).thenReturn(null);
        verify(bringConnectionMock2).openInputStream("http://optional?q=123456");
    }
}