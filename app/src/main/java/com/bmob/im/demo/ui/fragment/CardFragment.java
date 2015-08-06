package com.bmob.im.demo.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.bmob.im.demo.R;
import com.bmob.im.demo.adapter.CardListAdapter;
import com.bmob.im.demo.adapter.base.BaseListAdapter;
import com.bmob.im.demo.adapter.base.ViewHolder;
import com.bmob.im.demo.bean.Card;
import com.bmob.im.demo.bean.Question;
import com.bmob.im.demo.bean.Tool;
import com.bmob.im.demo.bean.User;
import com.bmob.im.demo.ui.CardItemActivityElinc;
import com.bmob.im.demo.ui.FragmentBase;
import com.bmob.im.demo.ui.QuestionItemActivityElinc;
import com.bmob.im.demo.util.CollectionUtils;
import com.bmob.im.demo.view.xlist.XListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.bmob.im.BmobUserManager;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.datatype.BmobRelation;
import cn.bmob.v3.listener.CountListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;

public class CardFragment extends FragmentBase implements XListView.IXListViewListener {
    private View view;
    private List<Card>cardList;
    private CardListAdapter myAdapter;
    private XListView listView;
    int curPage = 0;
    final int pageCapacity=2;
     public CardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_card, container, false);
        return view;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initList();
    }
    private void initList(){
        cardList = new ArrayList<>();
        listView = (XListView)findViewById(R.id.card_list);
        // 首先不允许加载更多
        listView.setPullLoadEnable(true);
        // 不允许下拉
        listView.setPullRefreshEnable(true);
        // 设置监听器
        listView.setXListViewListener(this);
        listView.pullRefreshing();
        listView.setDividerHeight(2);
        myAdapter = new CardListAdapter(getActivity(),cardList);
        listView.setAdapter(myAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                String cardId = cardList.get(position - 1).getObjectId();
                ShowToast("point" + position);
                bundle.putString("cardId", cardId);
                intent.putExtras(bundle);
                intent.setClass(getActivity(), CardItemActivityElinc.class);
                startAnimActivity(intent);
            }
        });
        refreshList();
    }
    private void refreshList(){
        BmobQuery<Card>query = new BmobQuery<>();
        query.order("-updatedAt");
        query.setLimit(pageCapacity*(curPage+1));
        query.include("goal");  // 希望在查询帖子信息的同时也把发布人的信息查询出来
        query.findObjects(getActivity(), new FindListener<Card>() {
            @Override
            public void onSuccess(List<Card> list) {
                if(list.size()!=cardList.size()) {
                    Log.i("listsize:",""+list.size());
                    cardList.clear();
                    cardList.addAll(list);
                    myAdapter.notifyDataSetChanged();
                }
                listView.stopRefresh();
            }
            @Override
            public void onError(int i, String s) {
                Toast.makeText(getActivity(), "打卡记录获取失败", Toast.LENGTH_SHORT).show();
                listView.stopRefresh();
            }
        });
    }
    @Override
    public void onResume() {
        super.onResume();
    }
    @Override
    public void onRefresh() {
        refreshList();
    }
    @Override
    public void onLoadMore() {
        BmobQuery<Card> query = new BmobQuery<>();
        query.setSkip((curPage + 1) * pageCapacity);
        query.setLimit(pageCapacity);
        query.order("-updatedAt");
        query.include("goal");  // 希望在查询帖子信息的同时也把发布人的信息查询出来
        query.findObjects(getActivity(), new FindListener<Card>() {
            @Override
            public void onSuccess(List<Card> list) {
                if (list.size() != 0) {
                    cardList.addAll(list);
                    curPage++;
                    myAdapter.notifyDataSetChanged();
                } else {
                    ShowToast("数据加载完成");
                    listView.setPullLoadEnable(false);
                    refreshLoad();
                }
            }

            @Override
            public void onError(int i, String s) {
                ShowToast("查询失败");
                refreshLoad();
            }
        });
    }
    private void refreshLoad(){
        if (listView.getPullLoading()) {
            listView.stopLoadMore();
        }
    }
}
