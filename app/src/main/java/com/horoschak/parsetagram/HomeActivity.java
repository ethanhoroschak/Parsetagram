package com.horoschak.parsetagram;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.horoschak.parsetagram.model.Post;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;

public class HomeActivity extends AppCompatActivity {

    // view objects
    @BindView(R.id.etDescription) EditText etDescription;
    @BindView(R.id.bCreate) Button bCreate;
    @BindView(R.id.bRefresh) Button bRefresh;
    @BindView(R.id.bLogout) Button bLogout;
    @BindView(R.id.bPhotos) ImageButton bPhotos;
    @BindView(R.id.bCamera) ImageButton bCamera;
    @BindView(R.id.ivImage) ImageView ivImage;
    // Request codes
    private final static int PICK_PHOTO_CODE = 1046;
    // needed values
    private File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        setOnClick();
    }

    //  TODO Set onclick listeners for create and refresh pre Butterknife
    private void setOnClick() {
        bCreate.setOnClickListener(view -> {
            final String description = etDescription.getText().toString();
            final ParseUser user = ParseUser.getCurrentUser();
            // TODO have user select or take photo
            //final File file = new File("/storage/emulated/0/DCIM/Camera/IMG_20190708_153100.jpg");
            final ParseFile parseFile = new ParseFile(file);
            // save first
            // TODO runtime persmissions for file
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
        });

        bRefresh.setOnClickListener(view -> loadTopPosts());
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

    @Optional
    @OnClick(R.id.bPhotos)
    public void OnClickPhotos() {
        // Create intent for picking a photo from the gallery
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getPackageManager()) != null) {
            // Bring up gallery to select a photo
            startActivityForResult(intent, PICK_PHOTO_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            Uri photoUri = data.getData();
            file = new File(getRealPathFromURI(photoUri));
            // Do something with the photo based on Uri
            Bitmap selectedImage = null;
            try {
                selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Load the selected image into a preview
            ivImage.setImageBitmap(selectedImage);
        }
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }
}
