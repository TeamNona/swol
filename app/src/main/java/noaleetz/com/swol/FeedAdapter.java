package noaleetz.com.swol;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.parse.ParseException;

import org.parceler.Parcels;
import org.w3c.dom.Text;

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

    public FeedAdapter(List<Workout> posts) {
        mposts = posts;
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
        Workout post = mposts.get(position);

        final RoundedCornersTransformation roundedCornersTransformation = new RoundedCornersTransformation(15,15);
        final RequestOptions requestOptions = RequestOptions.bitmapTransform(roundedCornersTransformation);

        try {
            Glide.with(mcontext)
                    .load(post.getMedia().getFile())
                    .into(holder.ivWorkoutImage);
        }
        catch (ParseException e) {
            e.printStackTrace();
        }

        holder.tvWorkoutTitle.setText(post.getName());
//        holder.tvLocation.setText(post.getLocation());

        holder.tvDescription.setText(post.getDescription());
//        holder.tvParticipants.setText(post.getParticipants());
        holder.tvCreatedBy.setText(post.getUser().getUsername());
//        holder.tvTime.setText(post.getTime());





    }

    @Override
    public int getItemCount() {

        return mposts.size();    }




    // create inner class for ViewHolder that extends to RecyclerView.ViewHolder

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // define and reference all the Views defined in our workout_item.xml file
        @BindView(R.id.btDetail)
        Button btDetail;
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
            ButterKnife.bind(itemView);


        }

        @Override
        public void onClick(View view) {
//            final Post post = mposts.get(getAdapterPosition());


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
