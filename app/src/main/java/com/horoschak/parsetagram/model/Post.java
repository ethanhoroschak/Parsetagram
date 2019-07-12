package com.horoschak.parsetagram.model;

import android.util.Log;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.Collections;
import java.util.List;

@ParseClassName("Post")
public class Post extends ParseObject {
    // parse columns
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_IMAGE = "image";
    public static final String KEY_USER = "user";
    public static final String KEY_CREATED_AT = "createdAt";
    public static final String KEY_LIKES = "likes";
    public static final String KEY_POST = "posts";
    public static final String KEY_LIKED_BY = "likedBy";

    //singleton, addlaalunique
    // Setters and Getters
    public String getDescription() {
        return getString(KEY_DESCRIPTION);
    }

    public void setDescription(String description) {
        put(KEY_DESCRIPTION, description);
    }

    public ParseFile getImage() {
        return getParseFile(KEY_IMAGE);
    }

    public void setImage(ParseFile image) {
        put(KEY_IMAGE, image);
    }

    public ParseUser getUser() {
        return getParseUser(KEY_USER);
    }

    public void setUser(ParseUser user) {
        put(KEY_USER, user);
    }

    public int getLikes() {
        List<ParseUser> users = getList(KEY_LIKED_BY);
        if (users == null) {
            return 0;
        }
        return users.size();
    }

    public void toggleLiked(ParseUser user) {
        Log.d("liked", String.valueOf(isLiked(user)));
        addAllUnique(KEY_LIKED_BY, Collections.singleton(user));
        Log.d("liked", String.valueOf(isLiked(user)));
    }

    public void toggleUnlike(ParseUser user) {
        Log.d("liked", String.valueOf(isLiked(user)));
        removeAll(KEY_LIKED_BY, Collections.singleton(user));
        Log.d("liked", String.valueOf(isLiked(user)));
    }

    public boolean isLiked(ParseUser user) {
        List<ParseUser> users = getList(KEY_LIKED_BY);
        boolean liked = false;
        if (users != null) {
            for (int i = 0; i < users.size(); i++) {
                if (user.getObjectId().equals(users.get(i).getObjectId())) {
                    liked = true;
                }
            }
        }
        return liked;
    }

    public int getPosts() {
        return getInt(KEY_POST);
    }

    public void setPosts() { put(KEY_POST, 0);}

    public void addPost() {
        int posts = getInt(KEY_POST);
        put(KEY_POST, posts + 1);
    }

    public void removePost() {
        int posts = getInt(KEY_POST);
        put(KEY_POST, posts - 1);
    }

    public static class Query extends ParseQuery<Post> {
        public Query() {
            super(Post.class);
        }

        public Query getTop() {
            setLimit(20);
            return this;
        }

        public Query setTop(int top) {
            setLimit(top);
            return this;
        }


        public Query withUser() {
            include("user");
            return this;
        }
    }
}
