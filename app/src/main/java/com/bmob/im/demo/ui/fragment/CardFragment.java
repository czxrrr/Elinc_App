package com.bmob.im.demo.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bmob.im.demo.R;
import com.bmob.im.demo.adapter.EmoViewPagerAdapter;
import com.bmob.im.demo.adapter.EmoteAdapter;
import com.bmob.im.demo.adapter.base.BaseListAdapter;
import com.bmob.im.demo.adapter.base.ViewHolder;
import com.bmob.im.demo.bean.Card;
import com.bmob.im.demo.bean.CardReply;
import com.bmob.im.demo.bean.FaceText;
import com.bmob.im.demo.bean.User;
import com.bmob.im.demo.ui.CardItemActivityElinc;
import com.bmob.im.demo.ui.FragmentBase;
import com.bmob.im.demo.util.CommonUtils;
import com.bmob.im.demo.util.FaceTextUtils;
import com.bmob.im.demo.view.EmoticonsEditText;
import com.bmob.im.demo.view.xlist.XListView;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.datatype.BmobRelation;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

public class CardFragment extends FragmentBase implements XListView.IXListViewListener{
    private View view;
    private List<Card>cardList;
    private CardFragment.CardListAdapter myAdapter;
    private XListView listView;
    private Button btn_chat_emo,btn_chat_send;
    private LinearLayout layout_more,layout_emo;
    private EmoticonsEditText edit_user_comment;
    private ViewPager pager_emo;
    private List<FaceText> emos;

    private int curPage = 0;
    private final int pageCapacity=10;
    private Card currentCard;
    private int currentPosition;

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
        view = inflater.inflate(R.layout.fragment_card, container, false);
        return view;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initList();
        initBottomView();
    }
    private void initList(){
        cardList = new ArrayList<>();
        listView = (XListView)findViewById(R.id.card_list);
        // 允许加载更多
        listView.setPullLoadEnable(true);
        // 允许下拉
        listView.setPullRefreshEnable(true);
        // 设置监听器
        listView.setXListViewListener(this);
        //设置下拉刷新
        listView.pullRefreshing();
        //设置divider高度
        listView.setDividerHeight(2);
        myAdapter = new CardFragment.CardListAdapter(getActivity(),cardList);
        listView.setAdapter(myAdapter);
        refreshList();
    }

    /**
     *  刷新列表，更新前 curPage 页数据
     */
    private void refreshList(){
        BmobQuery<Card>query = new BmobQuery<>();
        query.order("-updatedAt");
        query.setLimit(pageCapacity*(curPage+1));
        query.include("goal");  // 希望在查询帖子信息的同时也把发布人的信息查询出来
        query.findObjects(getActivity(), new FindListener<Card>() {
            @Override
            public void onSuccess(List<Card> list) {
                if (list.size() > 0) {
                    if (list.size() != cardList.size() || !list.get(0).getObjectId().equals(cardList.get(0).getObjectId())) {
                        Log.i("listsize:", "" + list.size());
                        cardList.clear();
                        cardList.addAll(list);
                        myAdapter.notifyDataSetChanged();
                        //listView.setOnItemClickListener(CardFragment.this);
                    }
                } else {
                    cardList.clear();
                    myAdapter.notifyDataSetChanged();
                    //listView.setOnItemClickListener(null);
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
    //上拉加载更多
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
                    //listView.setOnItemClickListener(CardFragment.this);
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
    //加载结束，取消可加载状态
    private void refreshLoad(){
        if (listView.getPullLoading()) {
            listView.stopLoadMore();
        }
    }


    private void initBottomView() {
        // 最左边
        btn_chat_emo = (Button)view.findViewById(R.id.btn_chat_emo);
        // 最右边
        btn_chat_send = (Button)view.findViewById(R.id.btn_chat_send);
        // 最下面
        layout_more = (LinearLayout)findViewById(R.id.layout_more);
        layout_emo = (LinearLayout)findViewById(R.id.layout_emo);
        initEmoView();
        // 输入框
        edit_user_comment = (EmoticonsEditText)findViewById(R.id.edit_user_comment);
    }
    //初始化表情包
    private void initEmoView() {
        pager_emo = (ViewPager)findViewById(R.id.pager_emo);
        emos = FaceTextUtils.faceTexts;
        List<View> views = new ArrayList<>();
        for (int i = 0; i < 2; ++i) {
            views.add(getGridView(i));
        }
        pager_emo.setAdapter(new EmoViewPagerAdapter(views));
    }
    //表情 grid view
    private View getGridView(final int i) {
        View view = View.inflate(getActivity(), R.layout.include_emo_gridview, null);
        GridView gridview = (GridView) view.findViewById(R.id.gridview);
        List<FaceText> list = new ArrayList<>();
        if (i == 0) {
            list.addAll(emos.subList(0, 21));
        } else if (i == 1) {
            list.addAll(emos.subList(21, emos.size()));
        }
        final EmoteAdapter gridAdapter = new EmoteAdapter(getActivity(), list);
        gridview.setAdapter(gridAdapter);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                FaceText name = (FaceText) gridAdapter.getItem(position);
                //key为表情的文字编码
                String key = name.text;
                try {
                    if (edit_user_comment != null && !TextUtils.isEmpty(key)) {
                        //start为插入前位置
                        int start = edit_user_comment.getSelectionStart();
                        //将key插入原edit_user_comment中
                        CharSequence content = edit_user_comment.getText().insert(start, key);
                        edit_user_comment.setText(content);
                        // 定位光标位置
                        CharSequence info = edit_user_comment.getText();
                        if (info instanceof Spannable) {
                            Spannable spanText = (Spannable) info;
                            Selection.setSelection(spanText, start + key.length());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return view;
    }

    /**
     * 根据是否点击笑脸来显示文本输入框的状态
     * @Title: showEditState
     * @Description:
     * @param isEmo: 用于区分文字和表情
     * @return void
     */
    private void showEditState(boolean isEmo) {
        edit_user_comment.setVisibility(View.VISIBLE);
        edit_user_comment.requestFocus();
        if (isEmo) {
            layout_more.setVisibility(View.VISIBLE);
            layout_emo.setVisibility(View.VISIBLE);
            hideSoftInputView();
        } else {
            layout_more.setVisibility(View.GONE);
            showSoftInputView();
        }
    }
    /** 隐藏软键盘
     * hideSoftInputView
     * @Title: hideSoftInputView
     * @Description:
     * @param
     * @return void
     */
    public void hideSoftInputView() {
        InputMethodManager manager = ((InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE));
        if (getActivity().getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getActivity().getCurrentFocus() != null)
                manager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
    // 显示软键盘
    public void showSoftInputView() {
        if (getActivity().getWindow().getAttributes().softInputMode == WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getActivity().getCurrentFocus() != null)
                ((InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE)).showSoftInput(edit_user_comment, 0);
        }
    }
    /**
     * Created by HUBIN on 2015/8/3.
     */
    public class CardListAdapter extends BaseListAdapter<Card> {
        public CardListAdapter(Context context, List<Card> list) {
            super(context, list);
        }

        @Override
        public View bindView(final int arg0, View convertView, ViewGroup arg2) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_card_in_list_elinc, null);
            }
            final Card contract = getList().get(arg0);
            final User me = BmobUser.getCurrentUser(mContext, User.class);
            TextView goal_content = ViewHolder.get(convertView, R.id.goal_content);
            TextView claim = ViewHolder.get(convertView, R.id.claim);

            goal_content.setText(contract.getGoal().getGoalContent());
            claim.setText(contract.getGoal().getClaim());

            final Button like = ViewHolder.get(convertView,R.id.add_like);
            like.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BmobQuery<User>query = new BmobQuery<>();
                    Card card = new Card();
                    card.setObjectId(contract.getObjectId());
                    query.addWhereRelatedTo("likedBy", new BmobPointer(card));
                    query.findObjects(mContext, new FindListener<User>() {
                        @Override
                        public void onSuccess(List<User> object) {
                            Log.i("life", "查询个数：" + object.size());
                            boolean like = false;
                            for (int i = 0; i < object.size(); i++) {
                                if (object.get(i).getObjectId().equals(me.getObjectId())) {
                                    like = true;
                                    break;
                                }
                            }
                            if (like) {
                                ShowToast("已经赞过了");
                            } else {
                                Card card = new Card();
                                card.setObjectId(contract.getObjectId());
                                card.increment("likedByNum");
                                BmobRelation likedBy = new BmobRelation();
                                likedBy.add(me);
                                card.setLikedBy(likedBy);
                                card.setObjectId(contract.getObjectId());
                                card.update(mContext, new UpdateListener() {
                                    @Override
                                    public void onSuccess() {
                                        ShowToast("点赞成功");
                                    }
                                    @Override
                                    public void onFailure(int i, String s) {
                                        Log.i("card_fragment", "点赞失败");
                                    }
                                });
                            }
                        }

                        @Override
                        public void onError(int code, String msg) {
                            Log.i("life", "查询失败：" + code + "-" + msg);
                        }
                    });
                }
            });

            final Button comment = ViewHolder.get(convertView,R.id.add_comment);
            comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i("click","btn_comment clicked");
                    //显示input栏
                    showEditState(false);
                    listView.setSelection(listView.getCount() - 1);
                    if (layout_more.getVisibility() == View.VISIBLE) {
                        layout_emo.setVisibility(View.GONE);
                        layout_more.setVisibility(View.GONE);
                    }else{
                        Log.i("click","input bar launch");

                        layout_more.setVisibility(View.VISIBLE);
                        edit_user_comment.setFocusable(true);
                        edit_user_comment.setFocusableInTouchMode(true);
                        edit_user_comment.requestFocus();
                        edit_user_comment.requestFocusFromTouch();
                    }
                    //设置监听
                    //发送评论
                    btn_chat_send.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final String msg = edit_user_comment.getText().toString();
                            if (msg.equals("")) {
                                ShowToast("请输入回复!");
                                return;
                            }
                            boolean isNetConnected = CommonUtils.isNetworkAvailable(getActivity());
                            if (!isNetConnected) {
                                ShowToast(R.string.network_tips+"");
                                return;
                            }
                            CardReply cardReply = new CardReply();
                            cardReply.setCard(cardList.get(arg0));
                            cardReply.setContent(msg);
                            User replyAuthor = BmobUser.getCurrentUser(getActivity(),User.class);
                            User replyTo = cardList.get(arg0).getGoal().getAuthor();
                            cardReply.setReplyAuthor(replyAuthor);
                            cardReply.setReplyTo(replyTo);
                            cardReply.save(getActivity(), new SaveListener() {
                                @Override
                                public void onSuccess() {
                                    ShowToast("发送成功");
                                    refreshList();
                                }
                                @Override
                                public void onFailure(int i, String s) {
                                    ShowToast("发送失败");
                                }
                            });
                        }
                    });
                    //选择表情
                    btn_chat_emo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (layout_more.getVisibility() == View.GONE) {
                                showEditState(true);
                            } else {
                                layout_more.setVisibility(View.GONE);
                            }
                        }
                    });
                }
            });

            return convertView;
        }
    }
}