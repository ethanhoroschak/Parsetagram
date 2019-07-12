package com.horoschak.parsetagram.fragments;

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

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.horoschak.parsetagram.HomeActivity;
import com.horoschak.parsetagram.R;
import com.horoschak.parsetagram.model.Post;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

// TODO put a back button and scroll to position rv where the thing was

public class DetailFragment extends Fragment {
    private Context context;
    private Unbinder unbinder;
    @BindView(R.id.ivProfileImage) ImageView ivProfileImage;
    @BindView(R.id.tvUser) TextView tvUser;
    @BindView(R.id.tvDate) TextView tvDate;
    @BindView(R.id.tvLikeCount) TextView tvLikeCount;
    @BindView(R.id.ivImage) ImageView ivImage;
    @BindView(R.id.ivLike) ImageView ivLike;
    @BindView(R.id.ivComment) ImageView ivComment;
    @BindView(R.id.ivMessage) ImageView ivMessage;
    @BindView(R.id.tvUser2) TextView tvUser2;
    @BindView(R.id.tvDescription) TextView tvDescription;
    @BindView(R.id.ibShare) ImageButton ibShare;
    private String likesString;
    private String objectId;
    private ParseUser user;
    private Post post;
    // The onCreateView method is called when Fragment should create its View object hierarchy,
    // either dynamically or via XML layout inflation.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        context = parent.getContext();
        user = ParseUser.getCurrentUser();
        Bundle bundle = this.getArguments();
        objectId = bundle.getString("post");
        //Toast.makeText(context, objectId, Toast.LENGTH_SHORT).show();
        return inflater.inflate(R.layout.fragment_detail, parent, false);
    }

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Setup any handles to view objects here
        unbinder = ButterKnife.bind(this, view);
        HomeActivity.toolbar.setVisibility(View.VISIBLE);
        likesString = "likes";
        //bRefresh = view.findViewById(R.id.bRefresh);
        loadPost();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    protected void loadPost() {
        // set up query
        final Post.Query postsQuery = new Post.Query();
        // Add Query specifications
        postsQuery
                .withUser()
                .whereEqualTo("objectId", objectId);
        postsQuery.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> objects, ParseException e) {
                if (e == null) {
                    post = objects.get(0);
                    bindView(post);
                    setOnLikeClicks(post);
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    private void bindView(Post post) {
        tvUser2.setText(post.getUser().getUsername());
        tvUser.setText(post.getUser().getUsername());
        tvDate.setText(getRelativeTimeAgo(post.getCreatedAt().toString()));
        if (post.getLikes() == 1) {
            likesString = "like";
        }
        tvLikeCount.setText(String.format("%,d", post.getLikes()) + " " + likesString);
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
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }

        return relativeDate;
    }

    public void updateLikes(Post post) {
        if (post.getLikes() == 1) {
            likesString = "like";
        } else {
            likesString = "likes";
        }
        tvLikeCount.setText(String.format("%,d", post.getLikes()) + " " + likesString);
    }

    // change onclick behavior depending on whether like is pressed
    public void setOnLikeClicks(Post post) {
        if (post.isLiked(user)) {
            ivLike.setImageResource(R.drawable.ic_iconfinder_heart_clicked);
            ivLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    post.toggleUnlike(user);
                    post.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(com.parse.ParseException e) {
                            if (e == null) {
                                Log.d("Liked", "unlike success");
                                updateLikes(post);
                                ivLike.setImageResource(R.drawable.ic_iconfinder_heart_unclick);
                                setOnLikeClicks(post);
                            } else {
                                e.printStackTrace();
                            }

                        }
                    });

                }
            });
        } else {
            ivLike.setImageResource(R.drawable.ic_iconfinder_heart_unclick);
            ivLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    post.toggleLiked(user);
                    post.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(com.parse.ParseException e) {
                            if (e == null) {
                                Log.d("Liked", "like success");
                                updateLikes(post);
                                ivLike.setImageResource(R.drawable.ic_iconfinder_heart_clicked);
                                setOnLikeClicks(post);
                            } else {
                                e.printStackTrace();
                            }

                        }
                    });

                }
            });
        }
    }
}
