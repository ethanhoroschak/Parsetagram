package com.horoschak.parsetagram.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.horoschak.parsetagram.GridAdapter;
import com.horoschak.parsetagram.HomeActivity;
import com.horoschak.parsetagram.MainActivity;
import com.horoschak.parsetagram.NewUserSetUp;
import com.horoschak.parsetagram.R;
import com.horoschak.parsetagram.SpacesItemDecoration;
import com.horoschak.parsetagram.model.Post;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class ProfileFragment extends Fragment {
    @BindView(R.id.rvPosts) RecyclerView rvPosts;
    @BindView(R.id.bLogout) Button bLogout;
    @BindView(R.id.ivProfileImage) ImageView ivProfileImage;
    @BindView(R.id.tvPosts) TextView tvPosts;
    @BindView(R.id.tvFollowers) TextView tvFollowers;
    @BindView(R.id.tvFollowing) TextView tvFollowing;
    @BindView(R.id.tvHandle) TextView tvHandle;
    @BindView(R.id.tvBio) TextView tvBio;
    private Context context;
    private Unbinder unbinder;
    protected GridAdapter adapter;
    protected List<Post> mPosts;
    private ParseUser user;

    // The onCreateView method is called when Fragment should create its View object hierarchy,
    // either dynamically or via XML layout inflation.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        context = parent.getContext();
        user = ParseUser.getCurrentUser();
        return inflater.inflate(R.layout.fragment_profile, parent, false);
    }

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Setup any handles to view objects here
        unbinder = ButterKnife.bind(this, view);
        HomeActivity.toolbar.setVisibility(View.GONE);
        // Set up recycler view
        mPosts = new ArrayList<>();
        adapter = new GridAdapter(context, mPosts);
        rvPosts.setAdapter(adapter);
        rvPosts.setLayoutManager(new GridLayoutManager(getActivity(), 3, RecyclerView.VERTICAL, false));
        SpacesItemDecoration decoration = new SpacesItemDecoration(5);
        rvPosts.addItemDecoration(decoration);
        //bRefresh = view.findViewById(R.id.bRefresh);
        loadUserData();
        loadProfilePosts();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    protected void loadProfilePosts() {
        // set up query
        final Post.Query postsQuery = new Post.Query();
        // Add Query specifications
        postsQuery
                .getTop()
                .withUser()
                .orderByDescending(Post.KEY_CREATED_AT)
                .whereEqualTo(Post.KEY_USER, ParseUser.getCurrentUser() );
        postsQuery.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> objects, ParseException e) {
                if (e == null) {
                    mPosts.addAll(objects);
                    adapter.notifyDataSetChanged();
                    for (int i = 0; i < objects.size(); i++) {
                        Log.d("HomeActivity", "Post{" + i + "}: " + objects.get(i).getDescription()
                                + "\nusername: " + objects.get(i).getUser().getUsername());
                    }
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    @OnClick(R.id.bEdit)
    public void onEditProfile() {
        Intent intent = new Intent(getActivity(), NewUserSetUp.class);
        startActivity(intent);
    }

    @OnClick(R.id.bLogout)
    public void OnLogout(Button button) {
        Log.d("LogoutActivity", "Logging out: " + user.getUsername());
        ParseUser.logOut();
        //ParseUser currentUser = ParseUser.getCurrentUser(); // this will now be null
        final Intent intent = new Intent(context, MainActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    private void loadUserData() {
        //String.format("%,d", post.getLikes())
        String id = user.getObjectId();
        tvPosts.setText(String.format("%,d", user.getInt("posts")));
        tvFollowers.setText(String.format("%,d", getFollowers()));
        tvFollowing.setText(String.format("%,d", getFollowing()));
        tvHandle.setText(user.getString("handle"));
        tvBio.setText(user.getString("bio"));
        ParseFile image = user.getParseFile("profileImage");
        if (image == null) {
            // placeholder image url
            String placeholder = "https://www.google.com/imgres?imgurl=http%3A%2F%2Fwww.agromarketday.com%2Fimages%2Fprofile-holder.png&imgrefurl=http%3A%2F%2Fwww.agromarketday.com%2Fmarkets&docid=xTH0MFkxJKZNlM&tbnid=7rahTNb3F7SPuM%3A&vet=10ahUKEwjJ-tD0h67jAhWKrlQKHZfxC9EQMwhZKAgwCA..i&w=800&h=800&safe=active&bih=723&biw=1596&q=profile%20image%20holder&ved=0ahUKEwjJ-tD0h67jAhWKrlQKHZfxC9EQMwhZKAgwCA&iact=mrc&uact=8";
            RequestOptions options = new RequestOptions();
            options.placeholder(R.drawable.profile_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .error(R.drawable.profile_placeholder)
                    .transform(new CenterCrop())
                    .transform(new CircleCrop());
            Glide.with(context)
                    .load(placeholder)
                    .apply(options) // Extra: round image corners
                    .into(ivProfileImage);
        } else {
            RequestOptions options = new RequestOptions();
            options.placeholder(R.drawable.profile_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .error(R.drawable.profile_placeholder)
                    .transform(new CenterCrop())
                    .transform(new CircleCrop());
            Glide.with(context)
                    .load(image.getUrl())
                    .apply(options) // Extra: round image corners
                    .into(ivProfileImage);
        }


    }

    public int getFollowers() {
        List<ParseUser> users = user.getList("followers");
        if (users == null) {
            return 0;
        }
        return users.size();
    }

    public int getFollowing() {
        List<ParseUser> users = user.getList("following");
        if (users == null) {
            return 0;
        }
        return users.size();
    }
}
