package com.horoschak.parsetagram;

import android.content.Context;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.horoschak.parsetagram.fragments.DetailFragment;
import com.horoschak.parsetagram.model.Post;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder>{
    // Data
    private Context context;
    private List<Post> posts;
    private ParseUser user;
    //private FragmentManager fragmentManager;

    public PostsAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        user = ParseUser.getCurrentUser();
        Post post = posts.get(position);
        holder.bind(post);
        // Open detail fragment
        holder.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new DetailFragment();
                Bundle bundle = new Bundle();
                bundle.putString("post", post.getObjectId());
                fragment.setArguments(bundle);
                HomeActivity.fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
            }
        });
        setOnLikeClicks(holder, post);
        //Log.d("list", post.getList("likedBy").toString());
    }

    // change onclick behavior depending on whether like is pressed
    public void setOnLikeClicks(ViewHolder holder, Post post) {
        if (post.isLiked(user)) {
            holder.ivLike.setImageResource(R.drawable.ic_iconfinder_heart_clicked);
            holder.ivLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    post.toggleUnlike(user);
                    post.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(com.parse.ParseException e) {
                            if (e == null) {
                                Log.d("Liked", "unlike success");
                                holder.updateLikes(post);
                                holder.ivLike.setImageResource(R.drawable.ufi_heart);
                                setOnLikeClicks(holder, post);
                            } else {
                                e.printStackTrace();
                            }

                        }
                    });

                }
            });
        } else {
            holder.ivLike.setImageResource(R.drawable.ufi_heart);
            holder.ivLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    post.toggleLiked(user);
                    post.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(com.parse.ParseException e) {
                            if (e == null) {
                                Log.d("Liked", "like success");
                                holder.updateLikes(post);
                                holder.ivLike.setImageResource(R.drawable.ic_iconfinder_heart_clicked);
                                setOnLikeClicks(holder, post);
                            } else {
                                e.printStackTrace();
                            }

                        }
                    });

                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivProfileImage;
        private TextView tvUser;
        private TextView tvDate;
        private TextView tvLikeCount;
        private ImageView ivImage;
        private ImageView ivLike;
        private ImageView ivComment;
        private ImageView ivMessage;
        private TextView tvUser2;
        private TextView tvDescription;
        private ImageButton ibShare;
        private String likesString;
        private ConstraintLayout root;

        // TODO put spannable for description and make username bold and clickable and share button
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvUser = itemView.findViewById(R.id.tvUser);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
            ivImage = itemView.findViewById(R.id.ivImage);
            ivLike = itemView.findViewById(R.id.ivLike);
            ivComment = itemView.findViewById(R.id.ivComment);
            ivMessage = itemView.findViewById(R.id.ivMessage);
            tvUser2 = itemView.findViewById(R.id.tvUser2);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            ibShare = itemView.findViewById(R.id.ibShare);
            root = itemView.findViewById(R.id.root);
            likesString = "likes";
        }

        public void bind(Post post) {
            tvUser2.setText(post.getUser().getUsername());
            tvUser.setText(post.getUser().getUsername());
            tvDate.setText(getRelativeTimeAgo(post.getCreatedAt().toString()));
            updateLikes(post);
            ParseFile image = post.getImage();
            if (image != null) {
                Glide.with(context)
                        .load(image.getUrl())
                        //.bitmapTransform(new RoundedCornersTransformation(context, 100, 0)) // Extra: round image corners
                        //.placeholder(R.color.orange_0) // Extra: placeholder for every image until load or error
                        //.error()
                        .into(ivImage);
            }
            tvDescription.setText(post.getDescription());
            ParseFile imageProfile = post.getUser().getParseFile("profileImage");
            if (imageProfile != null) {
                RequestOptions options = new RequestOptions();
                options.placeholder(R.drawable.profile_placeholder)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .error(R.drawable.profile_placeholder)
                        .transform(new CenterCrop())
                        .transform(new CircleCrop());
                Glide.with(context)
                        .load(imageProfile.getUrl())
                        .apply(options) // Extra: round image corners
                        .into(ivProfileImage);
            }
        }

        public void updateLikes(Post post) {
            if (post.getLikes() == 1) {
                likesString = "like";
            } else {
                likesString = "likes";
            }
            tvLikeCount.setText(String.format("%,d", post.getLikes()) + " " + likesString);
        }
    }

    // Format createAt date
    public String getRelativeTimeAgo(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        String relativeDate = "";
        try {
            long dateMillis = sf.parse(rawJsonDate).getTime();
            relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                    System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return relativeDate;
    }

    // Clean all elements of the recycler
    public void clear() {
        posts.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Post> list) {
        posts.addAll(list);
        notifyDataSetChanged();
    }
}
