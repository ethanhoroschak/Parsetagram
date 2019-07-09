package com.horoschak.parsetagram;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.horoschak.parsetagram.model.Post;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HomeActivity extends AppCompatActivity {

    // view objects
    @BindView(R.id.etDescription) EditText etDescription;
    @BindView(R.id.bCreate) Button bCreate;
    @BindView(R.id.bRefresh) Button bRefresh;
    @BindView(R.id.bLogout) Button bLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        bCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 final String description = etDescription.getText().toString();
                 final ParseUser user = ParseUser.getCurrentUser();
                 // TODO have user select or take photo
                 final File file = new File("/storage/emulated/0/DCIM/Camera/IMG_20190708_153100.jpg");
                 final ParseFile parseFile = new ParseFile(file);
                 // save first
                 parseFile.saveInBackground(new SaveCallback() {
                     @Override
                     public void done(ParseException e) {
                         if (e == null) {
                             createPost(description, parseFile, user);
                         } else {
                             e.printStackTrace();
                         }
                     }
                 });
            }
        });

        bRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadTopPosts();
            }
        });
    }

    private void createPost(String description, ParseFile imageFile, ParseUser user) {
        final Post newPost = new Post();
        newPost.setDescription(description);
        newPost.setImage(imageFile);
        newPost.setUser(user);

        newPost.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("HomeActivity", "post success");
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    private void loadTopPosts() {
        // set up query
        final Post.Query postsQuery = new Post.Query();
        // Add Query specifications
        postsQuery.getTop().withUser();
        postsQuery.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> objects, ParseException e) {
                if (e == null) {
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

    @OnClick(R.id.bLogout)
    public void OnLogout(Button button) {
        ParseUser currentUser = ParseUser.getCurrentUser();
        Log.d("LogoutActivity", "Logging out: " + currentUser.getUsername());
        ParseUser.logOut();
        //ParseUser currentUser = ParseUser.getCurrentUser(); // this will now be null
        final Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
