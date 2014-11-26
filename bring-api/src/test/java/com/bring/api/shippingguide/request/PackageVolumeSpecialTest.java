package com.bring.api.shippingguide.request;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PackageVolumeSpecialTest {
    private Package aPackage;
    
    @Before
    public void setUp() {
        aPackage = new Package();
        aPackage.withVolume("1337");
    }
    
    @Test
    public void should_not_set_volume_special_by_default() {
        String expected = "&volume=1337";

        assertEquals(expected, aPackage.toQueryString(""));
    }

    @Test
    public void should_not_set_volume_special_when_specified_false() {
        aPackage.withVolumeSpecial(false);
        String expected = "&volume=1337";

        assertEquals(expected, aPackage.toQueryString(""));
    }

    @Test
    public void should_set_volume_special_when_specified_true() {
        aPackage.withVolumeSpecial(true);
        String expected = "&volume=1337&volumeSpecial=true";

        assertEquals(expected, aPackage.toQueryString(""));
    }
}
