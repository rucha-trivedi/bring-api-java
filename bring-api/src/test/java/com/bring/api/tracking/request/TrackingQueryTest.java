package com.bring.api.tracking.request;

import com.bring.api.exceptions.MissingParameterException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TrackingQueryTest {
    TrackingQuery sqb;
    
    @Before
    public void setUp() {
        sqb = new TrackingQuery();
    }
    
    @Test(expected = MissingParameterException.class)
    public void shouldFailOnMissingSearchCriteria() {
        sqb.toQueryString();
    }
    
    @Test
    public void shouldGenerateQueryStringFromParameter() {
        sqb.withQueryNumber("123456");
        String str = sqb.toQueryString();
        assertEquals("?q=123456",str);
    }

    @Test
    public void should_set_optional_url(){
        assertNull(sqb.getOptionalUrl());
        assertFalse(sqb.hasOptionalUrl());
        sqb.withOptionalUrl("optional");
        assertEquals("optional", sqb.getOptionalUrl());
        assertTrue(sqb.hasOptionalUrl());
    }
}