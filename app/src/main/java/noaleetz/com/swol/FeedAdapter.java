package noaleetz.com.swol;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import noaleetz.com.swol.models.Workout;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.ViewHolder> {
    //Creating a global variable to store the array of string containing data to be displayed
    //Send dummy list that need to be displayed from MainActivity to MyListAdapter via constructor

    public List<Workout> mposts;
    private Context mcontext;

    private AdapterCallback mAdapterCallback;

    // implements interface that we have in adapter- called upon when user clicks on item in rv
    public interface AdapterCallback {
        void onMethodCallback(int position);
    }


    public FeedAdapter(List<Workout> posts) {
        mposts = posts;
    }

    public FeedAdapter(AdapterCallback callback) {
        this.mAdapterCallback = callback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // This defines a layout file for individual list item
        // Set the layout for individual list item inside onCreateViewHolder
        // Referencing the layout created for individual list item to get attached to RecyclerView
        mcontext = parent.getContext();

        LayoutInflater inflater = LayoutInflater.from(mcontext);

        View postView = inflater.inflate(R.layout.workout_item, parent, false);

        return new ViewHolder(postView);

    }

    @Override

    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        if (ParseUser.getCurrentUser() == null) {
            return;
        }

        final Workout post = mposts.get(position);

        final RoundedCornersTransformation roundedCornersTransformation = new RoundedCornersTransformation(15, 15);
        final RequestOptions requestOptions = RequestOptions.bitmapTransform(roundedCornersTransformation);




        try {
            Glide.with(mcontext)
                    .load(post.getMedia().getFile())
                    .into(holder.ivWorkoutImage);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.tvWorkoutTitle.setText(post.getName());

        // TODO- set icons and text in layout

        holder.tvLocation.setText(distanceFrom(post.getLocation()) + " mile(s) from you");

        holder.tvDescription.setText(post.getDescription());
//        holder.tvParticipants.setText(post.getParticipants());
        try {
            holder.tvCreatedBy.setText("Created By " + post.getUser().fetchIfNeeded().getUsername());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        holder.tvTime.setText(post.getTimeUntil());

        // call interface
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mAdapterCallback.onMethodCallback(position);
                ((MainActivity) mcontext).changeToDetailFragment(post);

            }
        });


    }

    // TODO- Configure current location accuracy

    public String distanceFrom(ParseGeoPoint workoutLocation) {
        ParseGeoPoint userLocation = ParseUser.getCurrentUser().getParseGeoPoint("currentLocation");
        double distance = workoutLocation.distanceInMilesTo(userLocation);
        return Double.toString(Math.round(distance));

    }

    @Override
    public int getItemCount() {

        return mposts.size();
    }


    // create inner class for ViewHolder that extends to RecyclerView.ViewHolder

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // define and reference all the Views defined in our workout_item.xml file
        @BindView(R.id.ivDetail)
        ImageView ivDetail;
        @BindView(R.id.ivWorkoutImage)
        ImageView ivWorkoutImage;
        @BindView(R.id.tvWorkoutTitle)
        TextView tvWorkoutTitle;
        @BindView(R.id.tvDescription)
        TextView tvDescription;
        @BindView(R.id.tvLocation)
        TextView tvLocation;
        @BindView(R.id.tvTime)
        TextView tvTime;
        @BindView(R.id.tvCreatedBy)
        TextView tvCreatedBy;
        @BindView(R.id.tvParticipants)
        TextView tvParticipants;


        public ViewHolder(View itemView) {


            super(itemView);

            itemView.setOnClickListener(this);
            ButterKnife.bind(this, itemView);


        }

        @Override
        public void onClick(View view) {





//            final Intent i = new Intent(mcontext,DetailActivity.class);
//            i.putExtra("post", Parcels.wrap(postObj));
//            mcontext.startActivity(i);
        }


    }

    // Clean all elements of the recycler
    public void clear() {
        mposts.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Workout> list) {
        mposts.addAll(list);
        notifyDataSetChanged();
    }



}
