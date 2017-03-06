package com.bring.api.tracking.response.v2;

import com.bring.api.tracking.response.TrackingResponse;
import no.bring.sporing._2.ConsignmentSet;

public class TrackingResultV2 implements TrackingResponse {
    private ConsignmentSet consignmentSet;

    public TrackingResultV2(ConsignmentSet consignmentSet) {
        this.consignmentSet = consignmentSet;
    }

    public ConsignmentSet getConsignmentSet() {
        return consignmentSet;
    }
}
