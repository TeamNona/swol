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

import noaleetz.com.swol.models.Workout;

class CustomWindowAdapter implements GoogleMap.InfoWindowAdapter {
    LayoutInflater mInflater;

    public CustomWindowAdapter(LayoutInflater i){
        mInflater = i;
    }

    // This defines the contents within the info window based on the marker
    @Override
    public View getInfoContents(Marker marker) {

//        Workout assigned_workout;




        // Getting view from the layout file
        View v = mInflater.inflate(R.layout.custom_info_window, null);
        // Populate fields

        Workout assigned_workout = (Workout) Parcels.unwrap((Parcelable) marker.getTag());

        TextView tvInfoTitle = v.findViewById(R.id.tvInfoTitle);
        tvInfoTitle.setText(assigned_workout.getName());

        TextView tvCreatedBy = v.findViewById(R.id.tvInfoCreatedBy);

        String user = null;
        try {
            user = assigned_workout.getUser().fetchIfNeeded().getUsername();
            tvCreatedBy.setText("Created By: "+ user);
        } catch (ParseException e) {
            e.printStackTrace();
        }


        TextView tvInfoTimeUntil = v.findViewById(R.id.tvInfoTimeUntil);
        tvInfoTimeUntil.setText(assigned_workout.getTimeUntil());

        ImageView ivInfoImage = v.findViewById(R.id.ivInfoImage);
        Glide.with(v).load(assigned_workout.getMedia().getUrl()).into(ivInfoImage);
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