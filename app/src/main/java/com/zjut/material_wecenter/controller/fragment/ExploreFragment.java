package com.zjut.material_wecenter.controller.fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nispok.snackbar.Snackbar;
import com.zjut.material_wecenter.Client;
import com.zjut.material_wecenter.R;
import com.zjut.material_wecenter.controller.activity.PostActivity;
import com.zjut.material_wecenter.controller.adapter.QuestionListAdapter;
import com.zjut.material_wecenter.models.Question;
import com.zjut.material_wecenter.models.Result;

import java.util.ArrayList;
import java.util.List;

public class ExploreFragment extends Fragment implements View.OnClickListener {

    // Loading state
    private boolean loading = true;
    private ArrayList<Question> mList;
    private QuestionListAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private View btnPublish;
    private Client client = Client.getInstance();

    private static int POST_ACTIVITY = 1;

    private int page = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_explore, container, false);
        // Init pulish button
        btnPublish = rootView.findViewById(R.id.button_publish);
        btnPublish.setOnClickListener(this);
        // Init swipe refresh layout
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_light,
                android.R.color.holo_red_light,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light);
        TypedValue typed_value = new TypedValue();
        getActivity().getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, typed_value, true);
        mSwipeRefreshLayout.setProgressViewOffset(false, 0, getResources().getDimensionPixelSize(typed_value.resourceId));
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                page = 1;
                new LoadQuestionList().execute();
            }
        });
        mSwipeRefreshLayout.setRefreshing(true);
        // Init recycler view
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.question_list);
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int visibleItemCount = mLayoutManager.getChildCount();
                int totalItemCount = mLayoutManager.getItemCount();
                int pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition();
                if (loading) {
                    if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                        // Load more news
                        loading = false;
                        mSwipeRefreshLayout.setRefreshing(true);
                        new LoadQuestionList().execute();
                    }
                }
            }
        });
        // Load question list
        new LoadQuestionList().execute();
        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_publish:
                Intent intent = new Intent(getActivity(), PostActivity.class);
                startActivityForResult(intent, POST_ACTIVITY);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == POST_ACTIVITY) {
            page = 1;
            new LoadQuestionList().execute();
        }
    }

    private class LoadQuestionList extends AsyncTask<Void, Integer, Integer> {
        private Result result;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            result = client.explore(page);
            if (result != null && result.getErrno() == 1) {
                if (page == 1) {
                    mList = (ArrayList<Question>) result.getRsm();
                } else {
                    mList.addAll((ArrayList<Question>) result.getRsm());
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            mSwipeRefreshLayout.setRefreshing(false);
            if (page == 1) {
                mAdapter = new QuestionListAdapter(getActivity(), mList);
                mRecyclerView.setAdapter(mAdapter);
            } else
                mAdapter.notifyDataSetChanged();
            page++;
            loading = true;
        }
    }
}
