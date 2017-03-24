package com.bring.api.tracking.request;

import com.bring.api.BringParser;
import com.bring.api.exceptions.RequestFailedException;
import com.bring.api.exceptions.UnmarshalException;
import com.bring.api.tracking.response.AbstractTrackingResponse;
import com.bring.api.tracking.response.v1.TrackingResult;
import no.bring.sporing._2.ConsignmentSet;

import java.io.InputStream;

public enum Version {
    v1("v1", new BringParser<TrackingResult>(TrackingResult.class)),
    v2("v2", new BringParser<ConsignmentSet>(ConsignmentSet.class));

    private String value;
    private BringParser parser;

    Version(String version, BringParser parser) {
        this.value = version;
        this.parser = parser;
    }

    public String getValue() {
        return value;
    }

    public AbstractTrackingResponse unmarshal(InputStream inputStream) throws UnmarshalException, RequestFailedException {
        if(is(v1)) {
            return (TrackingResult) this.parser.unmarshal(inputStream);
        }
        if (is(v2)) {
            return new com.bring.api.tracking.response.v2.TrackingResult((ConsignmentSet) this.parser.unmarshal(inputStream));
        }
        throw new RequestFailedException("not supported version : " + this.name(), 400);
    }

    public boolean is(Version version) {
        return this == version;
    }
}
