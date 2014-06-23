package com.bring.api.shippingguide.dao;

import com.bring.api.BringParser;
import com.bring.api.connection.BringConnection;
import com.bring.api.connection.HttpUrlConnectionAdapter;
import com.bring.api.shippingguide.request.Package;
import com.bring.api.shippingguide.request.QueryType;
import com.bring.api.shippingguide.request.Shipment;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ShippingGuideDaoTest {

    private ShippingGuideDao dao;
    com.bring.api.shippingguide.request.Package packet;
    private Shipment shipment;
    private BringConnection connectionMock;
    private BringParser parserMock;

    @Before
    public void setUp() throws Exception {
        dao = new ShippingGuideDao(new HttpUrlConnectionAdapter("test"));
        shipment = new Shipment();
        shipment.withFromPostalCode("1407");
        shipment.withToPostalCode("7050");
        packet = new Package();
        packet.withWeightInGrams("324");
        shipment.addPackage(packet);

        connectionMock = mock(BringConnection.class);
        parserMock = mock(BringParser.class);
    }

    @Test
    public void shouldUseOverriddenUrlIfSuppliedInRequest() throws Exception {
        when(connectionMock.openInputStream(anyString())).thenReturn(null);
        when(parserMock.unmarshal((InputStream) any())).thenReturn(null);

        shipment.withOverriddenUrl("http://some-test-url/fraktguide/products/");

        dao = new ShippingGuideDao(connectionMock, parserMock);
        dao.query(shipment, QueryType.PRICE);

        verify(connectionMock).openInputStream("http://some-test-url/fraktguide/products/price.xml" + shipment.toQueryString());
    }
}
