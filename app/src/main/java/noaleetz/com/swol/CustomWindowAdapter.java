package noaleetz.com.swol;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.parse.ParseException;

import org.parceler.Parcels;

class CustomWindowAdapter implements GoogleMap.InfoWindowAdapter {
    LayoutInflater mInflater;

    public CustomWindowAdapter(LayoutInflater i){
        mInflater = i;
    }

    // This defines the contents within the info window based on the marker
    @Override
    public View getInfoContents(Marker marker) {
        // Getting view from the layout file
        View v = mInflater.inflate(R.layout.custom_info_window, null);
        // Populate fields

        MapFragment.MarkerData data = Parcels.unwrap( (Parcelable) marker.getTag());

        TextView tvInfoTitle = v.findViewById(R.id.tvInfoTitle);
        tvInfoTitle.setText(data.getTitle());

        TextView tvCreatedBy = v.findViewById(R.id.tvInfoCreatedBy);
        tvCreatedBy.setText("Created By: "+ data.getCreatedBy());

        TextView tvInfoTimeUntil = v.findViewById(R.id.tvInfoTimeUntil);
        tvInfoTimeUntil.setText(data.getTimeUntil());

        ImageView ivInfoImage = v.findViewById(R.id.ivInfoImage);
        Glide.with(v).load(data.getImage()).into(ivInfoImage);
        // Return info window contents
        return v;
    }

    // This changes the frame of the info window; returning null uses the default frame.
    // This is just the border and arrow surrounding the contents specified above
    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }
}