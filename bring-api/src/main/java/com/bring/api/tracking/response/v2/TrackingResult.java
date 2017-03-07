package com.bring.api.tracking.response.v2;

import com.bring.api.tracking.response.TrackingResponse;
import no.bring.sporing._2.ConsignmentSet;

public class TrackingResult implements TrackingResponse {
    private ConsignmentSet consignmentSet;

    public TrackingResult(ConsignmentSet consignmentSet) {
        this.consignmentSet = consignmentSet;
    }

    public ConsignmentSet getConsignmentSet() {
        return consignmentSet;
    }
}
