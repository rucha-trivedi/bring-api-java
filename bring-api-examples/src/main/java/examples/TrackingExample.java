package examples;

import com.bring.api.BringService;
import com.bring.api.exceptions.RequestFailedException;
import com.bring.api.tracking.request.TrackingQuery;
import com.bring.api.tracking.response.v1.TrackingResult;

public class TrackingExample {
    public TrackingExample() throws RequestFailedException{
    	
        // Initialize library
        String clientId = "www.mywebshop.com";
        BringService bringService = new BringService(clientId);

        //Prepare query
        TrackingQuery query = new TrackingQuery();
        query.withQueryNumber("1234567");

        //Fetch Tracking information from Bring
        TrackingResult trackingResult = bringService.queryTracking(query);
        String totalWeight = trackingResult.getConsignment(0)
            .getTotalWeight()
            .getValue();

        //Display result
        System.out.println("Total weight:" + totalWeight);
    }
}
