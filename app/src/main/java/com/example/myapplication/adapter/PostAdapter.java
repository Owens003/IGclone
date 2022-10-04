package com.example.myapplication.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.service.autofill.SaveCallback;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.myapplication.R;
import com.example.myapplication.activity.CommentActivity;
import com.example.myapplication.activity.DetailActivity;
import com.example.myapplication.fragments.ProfileFragment;
import com.example.myapplication.help.TimeFormatter;
import com.example.myapplication.models.Post;
import com.example.myapplication.models.User;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import org.json.JSONException;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;


public class PostAdapter extends  RecyclerView.Adapter<PostAdapter.ViewHolder>{
    private final Context context;
    private final List<Post> posts;
    private static ArrayList<String> listLikes;


    public PostAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = posts.get(position);
        try {
            holder.bind(post);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {

        return posts.size();
    }

    public void clear(){
        posts.clear();
        notifyDataSetChanged();
    }

    // Method to add a list of Posts -- change to type used
    public void addAll(List<Post> postList){
        posts.addAll(postList);
        notifyDataSetChanged();
    }



    public  class ViewHolder extends RecyclerView.ViewHolder{

        private TextView tvUsername, tvDescription, tvLike, tvDate;
        private ImageView ivPhoto, profileImage;
        private ImageButton btnHeart, btnComment;
        private RelativeLayout containerProfile;
        int nbrLike;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.username);
            tvDescription = itemView.findViewById(R.id.description);
            tvLike = itemView.findViewById(R.id.tvLike);
            ivPhoto = itemView.findViewById(R.id.photo);
            profileImage = itemView.findViewById(R.id.profileImage);
            tvDate = itemView.findViewById(R.id.tvDate);
            containerProfile = itemView.findViewById(R.id.containerProfile);
            btnHeart = itemView.findViewById(R.id.btnHeart);
            btnComment = itemView.findViewById(R.id.btnComment);



        }

        public void bind(Post post) throws JSONException {
            ParseUser currentUser = ParseUser.getCurrentUser();
            listLikes = Post.fromJsonArray(post.getListLike());

            tvDescription.setText(post.getDescription());
            tvUsername.setText(post.getUser().getUsername());
            tvLike.setText(String.valueOf(post.getNumberLike()));
            tvDate.setText(TimeFormatter.getTimeStamp(post.getCreatedAt().toString()));

            ParseFile image = post.getImage();
            if(image != null){
                Glide.with(context).load(post.getImage().getUrl()) .centerCrop() // scale image to fill the entire ImageView
                        .transform(new RoundedCorners(30)).into(ivPhoto);
            }

            String profile_url = post.getUser().getParseFile(User.KEY_PROFILE_IMAGE).getUrl();
            Glide.with(context).load(profile_url) .centerCrop() // scale image to fill the entire ImageView
                    .into(profileImage);


            ivPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context, DetailActivity.class);
                    i.putExtra("post", Parcels.wrap(post));
                    context.startActivity(i);
                }
            });



            try{
                if (listLikes.contains(currentUser.getObjectId())) {
                    Drawable drawable = ContextCompat.getDrawable(context, R.drawable.ic_baseline_favorite_24);
                    btnHeart.setImageDrawable(drawable);
                }else {
                    Drawable drawable = ContextCompat.getDrawable(context, R.drawable.ic_baseline_favorite_border_24);
                    btnHeart.setImageDrawable(drawable);
                }
            }catch (NullPointerException e){
                e.printStackTrace();
            }

            btnComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context, CommentActivity.class);
                    i.putExtra("post", Parcels.wrap(post));
                    context.startActivity(i);
                }
            });

            btnHeart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    nbrLike = post.getNumberLike();
                    int index;

                    if (!listLikes.contains(currentUser.getObjectId())){
                        Drawable drawable = ContextCompat.getDrawable(context, R.drawable.ic_baseline_favorite_24);
                        btnHeart.setImageDrawable(drawable);
                        nbrLike++;
                        index = -1;

                    }else {
                        Drawable drawable = ContextCompat.getDrawable(context, R.drawable.ic_baseline_favorite_border_24);
                        btnHeart.setImageDrawable(drawable);
                        nbrLike--;
                        index = listLikes.indexOf(currentUser.getObjectId());
                    }

                    tvLike.setText(String.valueOf(nbrLike) + " likes");
                    saveLike(post, nbrLike, index, currentUser);
                }
            });



            containerProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();

                    // set parameters
                    ProfileFragment profileFragment = ProfileFragment.newInstance("Some Title");
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("post", Parcels.wrap(post));
                    profileFragment.setArguments(bundle);

                    fragmentManager.beginTransaction().replace(R.id.flContainer, profileFragment).commit();
                }
            });


        }

        private void saveLike(Post post, int nbrLike, int index, ParseUser currentUser) {
            post.setNumberLike(nbrLike);

            if (index == -1){
                post.setListLike(currentUser);
                listLikes.add(currentUser.getObjectId());
            }else {
                listLikes.remove(index);
                post.removeItemListLike(listLikes);
            }

            post.saveInBackground(new com.parse.SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null){
                        return;
                    }
                }
            });

        }
    }
}
