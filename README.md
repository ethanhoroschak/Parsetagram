# Project 3 - *Parsetagram*

**Parsetagram** is a photo sharing app using Parse as its backend.

Time spent: **22** hours spent in total

## User Stories

The following **required** functionality is completed:

- [x] User sees app icon in home screen.
- [x] User can sign up to create a new account using Parse authentication
- [x] User can log in and log out of his or her account
- [x] The current signed in user is persisted across app restarts
- [x] User can take a photo, add a caption, and post it to "Instagram"
- [x] User can view the last 20 posts submitted to "Instagram"
- [x] User can pull to refresh the last 20 posts submitted to "Instagram"
- [x] User can tap a post to view post details, including timestamp and caption.

The following **stretch** features are implemented:

- [x] Style the login page to look like the real Instagram login page.
- [x] Style the feed to look like the real Instagram feed.
- [x] User should switch between different tabs - viewing all posts (feed view), capture (camera and photo gallery view) and profile tabs (posts made) using a Bottom Navigation View.
- [x] User can load more posts once he or she reaches the bottom of the feed using infinite scrolling.
- [x] Show the username and creation time for each post
- [ ] After the user submits a new post, show an indeterminate progress bar while the post is being uploaded to Parse
- User Profiles:
  - [x] Allow the logged in user to add a profile photo
  - [x] Display the profile photo with each post
  - [ ] Tapping on a post's username or profile photo goes to that user's profile page
- [ ] User can comment on a post and see all comments for each post in the post details screen.
- [x] User can like a post and see number of likes for each post in the post details screen.
- [ ] Create a custom Camera View on your phone.
- [x] Run your app on your phone and use the camera to take the photo

The following **additional** features are implemented:

- [x] Shows a grid view of the user's posts on profile tab
- [x] Implemented Butterknife library to reduce boilerplate
- [x] Created an Edit Profile and New User Set Up activity that lets a user change profile photo, change the handle/name, and change the user's Bio
- [x] Styled the profile tab to mirror Instagram with number of posts, followers, following.
- [x] Made the backend/server models to handle follower and following arrays 

Please list two areas of the assignment you'd like to **discuss further with your peers** during the next class (examples include better ways to implement something, how to extend your app in certain ways, etc):

1. I would like to discuss way to implement the follower and following capability of the app. I made the data models in Parse and would like to create a front end for it. I would like to talk about how to change the home timeline to only include posts from followers and to add a follow button to user profiles. I would like to be able to see a list of followers and following users when you click on the text in the profile tab.
2. I would like to implement more UI/Style functionality on the each post in the timeline (comment, save, share, etc). I would like to discuss with people who did implement commenting about how the best way to model the data is. I would like to add a tagging feature to tag other users.

## Video Walkthrough

Here's a walkthrough of implemented user stories:

![](walkthrough.gif)

## Credits

List an 3rd party libraries, icons, graphics, or other assets you used in your app.

- [Android Async Http Client](http://loopj.com/android-async-http/) - networking library


## Notes

I spent a lot of time learning about fragments, how to pass data between fragments and activities, and how to start activities from fragments or fragments from fragments. Figuring out the best way to model the like system in the parse server took some trial and error before settling on an Array of Users.

## License

    Copyright [2019] [Ethan Horoschak]

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
