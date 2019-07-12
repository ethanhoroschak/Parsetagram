package com.horoschak.parsetagram;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.horoschak.parsetagram.fragments.DetailFragment;
import com.horoschak.parsetagram.model.Post;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.List;

public class GridAdapter extends RecyclerView.Adapter<GridAdapter.ViewHolder>{
    // Data
    private Context context;
    private List<Post> posts;
    private ParseUser user;
    //private FragmentManager fragmentManager;

    public GridAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_grid, parent, false);
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
        //Log.d("list", post.getList("likedBy").toString());
    }


    @Override
    public int getItemCount() {
        return posts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivImage;
        private ConstraintLayout root;

        // TODO put spannable for description and make username bold and clickable and share button
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivImage);
            root = itemView.findViewById(R.id.root);
        }

        public void bind(Post post) {
            ParseFile image = post.getImage();
            if (image != null) {
                Glide.with(context)
                        .load(image.getUrl())
                        //.bitmapTransform(new RoundedCornersTransformation(context, 100, 0)) // Extra: round image corners
                        //.placeholder(R.color.orange_0) // Extra: placeholder for every image until load or error
                        //.error()
                        .into(ivImage);
            }
        }
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
