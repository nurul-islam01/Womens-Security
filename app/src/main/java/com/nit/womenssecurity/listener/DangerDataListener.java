package com.nit.womenssecurity.listener;

import com.google.android.gms.maps.model.LatLng;
import com.nit.womenssecurity.pojos.User;

public interface DangerDataListener {
    void trackerProfile(User user);

    void trackerLocation(LatLng latLng);

}
