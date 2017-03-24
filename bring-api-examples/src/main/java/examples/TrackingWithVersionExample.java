package examples;

import com.bring.api.BringService;
import com.bring.api.exceptions.RequestFailedException;
import com.bring.api.tracking.request.TrackingQuery;
import com.bring.api.tracking.request.Version;
import com.bring.api.tracking.response.v2.TrackingResult;
import no.bring.sporing._2.PackageCargoConsignmentType;

public class TrackingWithVersionExample {
    public TrackingWithVersionExample() throws RequestFailedException {
        // Initialize library
        String clientId = "www.mywebshop.com";
        BringService bringService = new BringService(clientId);

        //Prepare query with version (by default it version 1)
        TrackingQuery query = new TrackingQuery();
        query.withQueryNumber("1234567");
        query.withOptionalVersion(Version.v2);

        //Fetch Tracking information from Bring
        TrackingResult trackingResult = (com.bring.api.tracking.response.v2.TrackingResult) bringService.queryTrackingWithVersion(query);
        String totalWeight = ((PackageCargoConsignmentType) trackingResult.getConsignmentSet().getConsignment().get(0))
                .getTotalWeight()
                .getValue();

        //Display result
        System.out.println("Total weight:" + totalWeight);
    }
}
