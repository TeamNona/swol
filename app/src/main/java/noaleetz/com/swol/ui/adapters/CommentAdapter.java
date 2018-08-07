package noaleetz.com.swol.ui.adapters;

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
import com.parse.ParseException;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import noaleetz.com.swol.ui.activities.MainActivity;
import noaleetz.com.swol.R;
import noaleetz.com.swol.models.Comments;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {


    private static final String TAG = "TAGCommentAdapter";
    public List<Comments> mcomments;
    private Context mcontext;
    String url;


    public CommentAdapter(List<Comments> comments) {
        mcomments = comments;

    }

    @NonNull
    @Override
    public CommentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        //Define a layout file for individual list item
// This defines a layout file for individual list item
        // Set the layout for individual list item inside onCreateViewHolder
        // Referencing the layout created for individual list item to get attached to RecyclerView
        mcontext = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(mcontext);
        View commentView = inflater.inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(commentView);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentAdapter.ViewHolder holder, int i) {
        //Set data to the individual list item
        final Comments comment = mcomments.get(i);


        try {
            holder.tvCommentUsername.setText(comment.getPostedBy().fetchIfNeeded().getUsername().toString());
        } catch (ParseException e) {
            e.printStackTrace();
            Log.d(TAG, "pointer to user not found");
        }
        holder.tvComment.setText(comment.get("description").toString());

        if (comment.getTimeUntil() == "In 0 seconds") {
            holder.tvCreatedTimeC.setText("0 seconds ago");


        } else {
            holder.tvCreatedTimeC.setText(comment.getTimeUntil());


        }
        // Load commenter avatar
        try {
            url = comment
                    .fetchIfNeeded()
                    .getParseUser("postedBy")
                    .getParseFile("profilePicture")
                    .getUrl();
        } catch (ParseException e) {
            e.printStackTrace();
            Log.d(TAG, "AvatarImage did not load");
        }

        Glide.with(mcontext)
                .load(url)
                .into(holder.ivAvatar);

        holder.ivAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mAdapterCallback.onMethodCallback(position);
                ((MainActivity) mcontext).changeToProfileFragment(comment.getPostedBy());

            }
        });
        holder.tvCommentUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mAdapterCallback.onMethodCallback(position);
                ((MainActivity) mcontext).changeToProfileFragment(comment.getPostedBy());

            }
        });


    }

    @Override
    public int getItemCount() {
        //Return the number of items in your list

        return mcomments.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.ivAvatar)
        ImageView ivAvatar;
        @BindView(R.id.tvCommentUsername)
        TextView tvCommentUsername;
        @BindView(R.id.tvComment)
        TextView tvComment;
        @BindView(R.id.tvCreatedTimeC)
        TextView tvCreatedTimeC;


        public ViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);
            ButterKnife.bind(this, itemView);

        }

        @Override
        public void onClick(View view) {
            // TODO- setup link to comment thread when clicked
        }
    }


    // Clean all elements of the recycler
    public void clear() {
        mcomments.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Comments> list) {
        mcomments.addAll(list);
        notifyDataSetChanged();
    }
}
