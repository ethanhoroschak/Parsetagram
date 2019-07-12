package com.horoschak.parsetagram.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.horoschak.parsetagram.EndlessRecyclerViewScrollListener;
import com.horoschak.parsetagram.HomeActivity;
import com.horoschak.parsetagram.PostsAdapter;
import com.horoschak.parsetagram.R;
import com.horoschak.parsetagram.model.Post;
import com.parse.FindCallback;
import com.parse.ParseException;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class HomeFragment extends Fragment {
    //@BindView(R.id.bRefresh) Button bRefresh;
    @BindView(R.id.rvPosts) RecyclerView rvPosts;
    @BindView(R.id.swipeContainer) SwipeRefreshLayout swipeContainer;


    private Context context;
    private Unbinder unbinder;
    protected PostsAdapter adapter;
    protected List<Post> mPosts;
    private EndlessRecyclerViewScrollListener scrollListener;

    // The onCreateView method is called when Fragment should create its View object hierarchy,
    // either dynamically or via XML layout inflation.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        context = parent.getContext();
        return inflater.inflate(R.layout.fragment_home, parent, false);
    }

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Setup any handles to view objects here
        unbinder = ButterKnife.bind(this, view);
        HomeActivity.toolbar.setVisibility(View.VISIBLE);
        // Set up recycler view
        // Make sure the toolbar exists in the activity and is not null
        mPosts = new ArrayList<>();
        adapter = new PostsAdapter(context, mPosts);
        rvPosts.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rvPosts.setLayoutManager(linearLayoutManager);
        // Retain an instance so that you can call `resetState()` for fresh searches
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                loadNextDataFromApi(page);
            }
        };
        // Adds the scroll listener to RecyclerView
        rvPosts.addOnScrollListener(scrollListener);
        //bRefresh = view.findViewById(R.id.bRefresh);
        loadTopPosts();
        setUpRefresh();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    //@OnClick(R.id.bRefresh)
    public void OnClickRefresh() {
        loadTopPosts();
    }

    protected void loadTopPosts() {
        // set up query
        final Post.Query postsQuery = new Post.Query();
        // Add Query specifications
        postsQuery.getTop().withUser().orderByDescending(Post.KEY_CREATED_AT);
        postsQuery.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> objects, ParseException e) {
                if (e == null) {
                    mPosts.addAll(objects);
                    adapter.notifyDataSetChanged();
                    for (int i = 0; i < objects.size(); i++) {
                        Log.d("HomeActivity", "Post{" + i + "}: " + objects.get(i).getDescription() +
                                objects.get(i).getLikes()
                                + "\nusername: " + objects.get(i).getUser().getUsername());
                    }
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    // Swipe container set up
    public void setUpRefresh() {
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                fetchTimelineAsync(0);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    public void fetchTimelineAsync(int page) {
        // Send the network request to fetch the updated data
        // `client` here is an instance of Android Async HTTP
        // getHomeTimeline is an example endpoint.
        final Post.Query postsQuery = new Post.Query();
        // Add Query specifications
        postsQuery.getTop().withUser().orderByDescending(Post.KEY_CREATED_AT);
        postsQuery.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> objects, ParseException e) {
                if (e == null) {
                    // Remember to CLEAR OUT old items before appending in the new ones
                    adapter.clear();
                    // Now we call setRefreshing(false) to signal refresh has finished
                    mPosts.addAll(objects);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(context, "refresh", Toast.LENGTH_SHORT).show();
                    for (int i = 0; i < objects.size(); i++) {
                        Log.d("HomeActivity", "Post{" + i + "}: " + objects.get(i).getDescription() +
                                objects.get(i).getLikes()
                                + "\nusername: " + objects.get(i).getUser().getUsername());
                    }
                    swipeContainer.setRefreshing(false);
                    scrollListener.resetState();
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    // Append the next page of data into the adapter
    // This method probably sends out a network request and appends new data items to your adapter.
    public void loadNextDataFromApi(int offset) {
        Log.d("data", String.valueOf(offset));
        // set up query
        final Post.Query postsQuery = new Post.Query();
        // Add Query specifications
        postsQuery.setTop(offset*20 + 20).withUser().orderByDescending(Post.KEY_CREATED_AT).setSkip(offset*20);
        postsQuery.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> objects, ParseException e) {
                if (e == null) {
                    mPosts.addAll(objects);
                    adapter.notifyDataSetChanged();
                    for (int i = 0; i < objects.size(); i++) {
                        Log.d("HomeActivity", "Post{" + i + "}: " + objects.get(i).getDescription() +
                                objects.get(i).getLikes()
                                + "\nusername: " + objects.get(i).getUser().getUsername());
                    }
                } else {
                    e.printStackTrace();
                }
            }
        });
    }
}
