package com.bring.api.tracking.response;

import com.bring.api.BringParser;
import com.bring.api.exceptions.UnmarshalException;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

public class BringParserTrackingHandlingAddressTest {
    InputStream recipientHandlingXml;
    TrackingResult consignmentSetInnlogget;
    Package innloggetPackage;

    @Before
    public void setUp() throws FileNotFoundException, UnmarshalException {
        BringParser<TrackingResult> parser = new BringParser<TrackingResult>(TrackingResult.class);

        recipientHandlingXml = getClass().getResourceAsStream("recipientHandlingAddress.xml");

        consignmentSetInnlogget = parser.unmarshal(recipientHandlingXml);
        innloggetPackage = consignmentSetInnlogget.getConsignments().get(0).getPackageSet().getPackages().get(0);
    }
    
    @Test
    public void shouldParseSendersNameFromInnloggetXml() {
        assertEquals("POSTEN NORGE AS POST BEDRIFTSSENTER POSTHUSET", innloggetPackage.getSenderName());
    }

    @Test
    public void shouldParseRecipientNameFromInnloggetXml() {
        assertEquals("LINE NORDMANN", innloggetPackage.getRecipientName());
    }

    @Test
    public void shouldParseRecipientAddressAddressLine1FromInnloggetXml() {
        assertEquals("MORTENSRUDVEIEN 15D", innloggetPackage.getRecipientHandlingAddress().getAddressLine1());
    }

    @Test
    public void shouldParseRecipientAddressAddressLine2FromInnloggetXml() {
        assertEquals("Adresselinje 2", innloggetPackage.getRecipientHandlingAddress().getAddressLine2());
    }

    @Test
    public void shouldParseRecipientAddressCityFromInnloggetXml() {
        assertEquals("OSLO", innloggetPackage.getRecipientHandlingAddress().getCity());
    }

    @Test
    public void shouldParseRecipientAddressCountryCodeFromInnloggetXml() {
        assertEquals("NO", innloggetPackage.getRecipientHandlingAddress().getCountryCode());
    }

    @Test
    public void shouldParseRecipientAddressCountryFromInnloggetXml() {
        assertEquals("Norway", innloggetPackage.getRecipientHandlingAddress().getCountry());
    }

}