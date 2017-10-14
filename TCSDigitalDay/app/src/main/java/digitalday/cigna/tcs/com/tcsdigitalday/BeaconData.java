package digitalday.cigna.tcs.com.tcsdigitalday;

import java.util.ArrayList;

/**
 * Created by venkatesh on 10/12/2017.
 */

public class BeaconData {
    String mversion,location;

    public String getMversion() {
        return this.mversion;
    }

    public void setMversion(String mversion) {
        this.mversion = mversion;
    }

    public String getGeoLocation() {
        return this.location;
    }

    public void setGeoLocation(String location) {
        this.location = location;
    }
}
