package com.example.myapplication.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.myapplication.help.EndlessRecyclerViewScrollListener;
import com.example.myapplication.models.Post;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.adapter.PostAdapter;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;


public class PostFragment extends Fragment {
    public static final String TAG = "PostFragment";
    private RecyclerView rvPost;
    private PostAdapter adapter;
    private List<Post> allPost;
    private SwipeRefreshLayout swipeContainer;
    private static ParseQuery<Post> query;
    private EndlessRecyclerViewScrollListener scrollListener;
    Context context;


    public PostFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_post, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvPost = view.findViewById(R.id.Post);
        swipeContainer = view.findViewById(R.id.swipeContainer);

        allPost = new ArrayList<>();
        adapter = new PostAdapter(getContext(), allPost);

        rvPost.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        rvPost.setLayoutManager(layoutManager);

        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.i(TAG, "Fetching new data!! (setOnRefreshListener)");
                queryPosts();
            }
        });


        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                Log.i(TAG, "Load more data "+page);
                loadMoreData();
            }
        };

//      Adds the scroll listener to RecyclerView
        rvPost.addOnScrollListener(scrollListener);
        queryPosts();
    }


    private void queryPosts() {
        query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);

        query.setLimit(20);
        query.addDescendingOrder(Post.KEY_CREATED_AT);

        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                if (e != null){
                    Log.i(TAG,"Issue with getting posts",e);
                     return;
                }

                adapter.clear();
                adapter.addAll(posts);
                swipeContainer.setRefreshing(false);
            }
        });
    }


    private void loadMoreData() {
        query = ParseQuery.getQuery(Post.class);

        query.include(Post.KEY_USER);
        query.setLimit(20);

        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                if (e != null){
                    Log.e(TAG, "Issue with getting Posts", e);
                    Toast.makeText(getContext(), "Issue with getting Posts", Toast.LENGTH_SHORT).show();
                    return;
                }

                adapter.addAll(posts);
            }
        });
    }

}