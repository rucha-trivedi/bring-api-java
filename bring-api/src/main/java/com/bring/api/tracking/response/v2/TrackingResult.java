package com.bring.api.tracking.response.v2;

import com.bring.api.tracking.response.AbstractTrackingResponse;
import no.bring.sporing._2.ConsignmentSet;

public class TrackingResult extends AbstractTrackingResponse {
    private ConsignmentSet consignmentSet;

    public TrackingResult(ConsignmentSet consignmentSet) {
        this.consignmentSet = consignmentSet;
    }

    public ConsignmentSet getConsignmentSet() {
        return consignmentSet;
    }
}
