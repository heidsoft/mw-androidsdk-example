package com.magicwindow.deeplink.fragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.magicwindow.deeplink.R;
import com.magicwindow.deeplink.adapter.NewsRecycleAdapter;
import com.magicwindow.deeplink.app.BaseFragment;
import com.magicwindow.deeplink.config.Config;
import com.magicwindow.deeplink.domain.NewsList;
import com.magicwindow.deeplink.task.NetTask;
import com.magicwindow.deeplink.ui.DividerItemDecoration;

import cn.salesuite.saf.inject.Injector;
import cn.salesuite.saf.inject.annotation.InjectView;
import cn.salesuite.saf.log.L;
import cn.salesuite.saf.rxjava.RxAsyncTask;

/**
 * Created by Tony Shen on 15/11/25.
 */
public class NewsDetailFragment extends BaseFragment {

    private static String STYLE = "style";

    @InjectView(id = R.id.news_detail_list)
    RecyclerView recyclerView;

    NewsRecycleAdapter adapter;
    int STYLE_VALUE = 0;
    NewsList list = null;

    public static NewsDetailFragment newInstance(int style) {
        NewsDetailFragment fragment = new NewsDetailFragment();
        Bundle args = new Bundle();
        args.putInt(STYLE, style);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (isVisibleToUser) {
            L.e("NewsDetailFragment visible");
//            TrackAgent.currentEvent().onPageStart("主 页");

        } else {
            L.e("NewsDetailFragment invisible");
//            TrackAgent.currentEvent().onPageEnd("主 页");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news_detail, container, false);
        Injector.injectInto(this, view);

        STYLE_VALUE = getArguments().getInt(STYLE, 0);

        return view;
    }

    @Override
    public void initView() {

        if (app.session.get(Config.newsList) != null) {
            list = (NewsList) app.session.get(Config.newsList);
            setRecyclerView();
        } else {
            NetTask task = new NetTask(Config.newsList);
            task.execute(new RxAsyncTask.HttpResponseHandler() {
                @Override
                public void onSuccess(String s) {
                    if (s != null && s.startsWith("{")) {
                        appPrefs.saveJson(Config.newsList, s);
                        list = appPrefs.getNewsList();
                        app.session.put(Config.newsList, list);
                        setRecyclerView();
                    }
                }

                @Override
                public void onFail(Throwable throwable) {
                    list = appPrefs.getNewsList();
                    setRecyclerView();
                }
            });
        }
    }

    private void setRecyclerView() {
        //新闻页面第一个魔窗位Config.MWS[46]
        list.internetList.get(0).mwKey = 46;
        list.sportList.get(0).mwKey = 46 + list.internetList.size();
        list.entertainmentList.get(0).mwKey = 46 + list.internetList.size() + list.sportList.size();

        switch (STYLE_VALUE) {
            default:
            case 0:
                adapter = new NewsRecycleAdapter(list.internetList);
                break;

            case 1:

                adapter = new NewsRecycleAdapter(list.sportList);
                break;

            case 2:
                adapter = new NewsRecycleAdapter(list.entertainmentList);
                break;

        }

        recyclerView.setAdapter(adapter);
        LinearLayoutManager mgr = new LinearLayoutManager(mContext);
        mgr.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(mgr);

        recyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST));
    }

}
