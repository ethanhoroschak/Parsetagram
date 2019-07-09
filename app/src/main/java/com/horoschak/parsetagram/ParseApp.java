package com.horoschak.parsetagram;

import android.app.Application;

import com.horoschak.parsetagram.model.Post;
import com.parse.Parse;
import com.parse.ParseObject;

public class ParseApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ParseObject.registerSubclass(Post.class);
        final Parse.Configuration configuration = new Parse.Configuration.Builder(this)
                .applicationId("ethan-insta")
                .clientKey("unsweetened-green-tea")
                .server("https://ehoroschak-fbu-instagram.herokuapp.com/parse")
                .build();
        Parse.initialize(configuration);
    }
}
