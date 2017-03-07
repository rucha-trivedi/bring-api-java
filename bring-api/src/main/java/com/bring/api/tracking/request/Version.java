package com.bring.api.tracking.request;

import com.bring.api.BringParser;
import com.bring.api.tracking.response.TrackingResponse;
import com.bring.api.tracking.response.v1.TrackingResult;
import no.bring.sporing._2.ConsignmentSet;

public enum Version {
    v1(new BringParser<TrackingResult>(TrackingResult.class)),
    v2(new BringParser<ConsignmentSet>(ConsignmentSet.class));

    private BringParser<TrackingResponse> parser;

    Version(BringParser parser) {
        this.parser = parser;
    }

    public BringParser getParser() {
        return parser;
    }
}
