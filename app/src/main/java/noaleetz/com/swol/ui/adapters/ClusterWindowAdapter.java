package noaleetz.com.swol.ui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import noaleetz.com.swol.R;
import noaleetz.com.swol.models.Workout;

public class ClusterWindowAdapter extends RecyclerView.Adapter<ClusterWindowAdapter.ViewHolder> {
    //Creating a global variable to store the array of string containing data to be displayed
    //Send dummy list that need to be displayed from MainActivity to MyListAdapter via constructor

    public List<Workout> workouts;
    private Context context;
    private View postView;
    private itemClickListener listener;


    public ClusterWindowAdapter(List<Workout> workouts) {
        this.workouts = workouts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // This defines a layout file for individual list item
        // Set the layout for individual list item inside onCreateViewHolder
        // Referencing the layout created for individual list item to get attached to RecyclerView
        context = parent.getContext();

        if (context instanceof itemClickListener) {
            listener = (itemClickListener) context;
        } else {
            throw new ClassCastException(parent.toString()
                    + " must implement ClusterWindowAdapter.itemClickListener");
        }


        LayoutInflater inflater = LayoutInflater.from(context);

        postView = inflater.inflate(R.layout.custom_info_window, parent, false);

        return new ViewHolder(postView);

    }

    @Override

    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        if (ParseUser.getCurrentUser() == null) {
            return;
        }

        final Workout workout = workouts.get(position);

        final RoundedCornersTransformation roundedCornersTransformation = new RoundedCornersTransformation(15, 15);
        final RequestOptions requestOptions = RequestOptions.bitmapTransform(roundedCornersTransformation);


        try {
            Glide.with(context)
                    .load(workout.getMedia().getFile())
                    .into(holder.ivInfoImage);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.tvInfoTitle.setText(workout.getName());

        // TODO- set icons and text in layout
        try {
            holder.tvInfoCreatedBy.setText("Created By " + workout.getUser().fetchIfNeeded().getUsername());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.tvInfoTimeUntil.setText(workout.getTimeUntil());

        // call interface
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: make it zoom in
                listener.onWorkoutSelected(workout);

            }
        });


    }

    @Override
    public int getItemCount() {
        return workouts.size();
    }


    // create inner class for ViewHolder that extends to RecyclerView.ViewHolder

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // define and reference all the Views defined in our workout_item.xml file
        @BindView(R.id.tvInfoCreatedBy)
        TextView tvInfoCreatedBy;
        @BindView(R.id.tvInfoTimeUntil)
        TextView tvInfoTimeUntil;
        @BindView(R.id.tvInfoTitle)
        TextView tvInfoTitle;
        @BindView(R.id.ivInfoImage)
        ImageView ivInfoImage;


        public ViewHolder(View itemView) {


            super(itemView);

            itemView.setOnClickListener(this);
            ButterKnife.bind(this, itemView);


        }

        @Override
        public void onClick(View view) {
            Toast.makeText(view.getContext(), "Click Click!!!", Toast.LENGTH_SHORT).show();
        }


    }

    // Clean all elements of the recycler
    public void clear() {
        workouts.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Workout> list) {
        workouts.addAll(list);
        notifyDataSetChanged();
    }

    public interface itemClickListener {
        void onWorkoutSelected(Workout workout);
    }
}
