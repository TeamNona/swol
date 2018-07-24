package noaleetz.com.swol;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseUser;

import org.json.JSONArray;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import noaleetz.com.swol.models.User;
import noaleetz.com.swol.models.Workout;

public class ParticipantAdapter extends RecyclerView.Adapter<ParticipantAdapter.ViewHolder> {

    private static final String TAG = "TAGParticipantAdapter";
    public List<ParseUser> mparticipants;
    private Context mcontext;
    String url;


    public ParticipantAdapter(List<ParseUser> participants){
        mparticipants = participants;

    }

    @NonNull
    @Override
    public ParticipantAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        // This defines a layout file for individual list item
        // Set the layout for individual list item inside onCreateViewHolder
        // Referencing the layout created for individual list item to get attached to RecyclerView
        mcontext = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(mcontext);
        View participantView = inflater.inflate(R.layout.participant_item,parent,false);
        return new ViewHolder(participantView);
    }

    @Override
    public void onBindViewHolder(@NonNull ParticipantAdapter.ViewHolder holder, int i) {
        final ParseUser participant = mparticipants.get(i);


        final RoundedCornersTransformation roundedCornersTransformation = new RoundedCornersTransformation(15,15);
        final RequestOptions requestOptions = RequestOptions.bitmapTransform(roundedCornersTransformation);

        holder.tvFullName.setText(participant.getString("name"));
        holder.tvUsername.setText(participant.getUsername());

        // Load user avatar
        try {
            url = participant
                    .fetchIfNeeded()
                    .getParseFile("profilePicture")
                    .getUrl();
        } catch (ParseException e) {
            e.printStackTrace();
            Log.d(TAG, "AvatarImage did not load");
        }

        Glide.with(mcontext)
                .load(url)
                .apply(requestOptions)
                .into(holder.ivAvatar);





    }

    @Override
    public int getItemCount() {
        return mparticipants.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.ivAvatar)
        ImageView ivAvatar;
        @BindView(R.id.tvFullName)
        TextView tvFullName;
        @BindView(R.id.tvUsername)
        TextView tvUsername;

        public ViewHolder (View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);
            ButterKnife.bind(this,itemView);

        }

        @Override
        public void onClick(View view) {
            // TODO- setup link to participants profile when clicked
        }
    }



    // Clean all elements of the recycler
    public void clear() {
        mparticipants.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<ParseUser> list) {
        mparticipants.addAll(list);
        notifyDataSetChanged();
    }

}
