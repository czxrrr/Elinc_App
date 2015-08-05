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
import android.view.MotionEvent;
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
    private LinearLayout layout_more,layout_emo,layout_input_bar;
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
    //初始化基本元素
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

    //刷新列表，更新前 curPage 页数据
    private void refreshList(){
        BmobQuery<Card>query = new BmobQuery<>();
        query.order("-updatedAt");
        curPage = 0;
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
                    }
                } else {
                    cardList.clear();
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
    //刷新列表
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
                if (list.size() != 0) { //拉取到新的数据
                    cardList.addAll(list);
                    curPage++;
                    myAdapter.notifyDataSetChanged();
                } else {                    //数据全部拉取完
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

    //初始化底部隐藏input bar
    private void initBottomView() {
        // 最左边
        btn_chat_emo = (Button)view.findViewById(R.id.btn_chat_emo);
        // 最右边
        btn_chat_send = (Button)view.findViewById(R.id.btn_chat_send);
        // 最下面
        layout_input_bar = (LinearLayout)findViewById(R.id.input_bar);
        layout_more = (LinearLayout)findViewById(R.id.layout_more);
        layout_emo = (LinearLayout)findViewById(R.id.layout_emo);
        initEmoView();      //初始化表情选择器
        // 输入框
        edit_user_comment = (EmoticonsEditText)findViewById(R.id.edit_user_comment);
        //设置当input bar失去焦点（点击list view时）隐藏输入法与评论栏
        listView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                hideSoftInputView();
                layout_input_bar.setVisibility(View.GONE);
                layout_more.setVisibility(View.GONE);
                return false;
            }
        });
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
    //表情包 grid view
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
     * 设定点击笑脸时的动作
     *  isEmo为true表示显示表情列表，
     *  isEmo为false表示收回表情列表
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
    // 隐藏软键盘
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
     *  card_fragment专用适配器
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
            TextView created_at = ViewHolder.get(convertView,R.id.created_at);
            final LinearLayout comment_layout = ViewHolder.get(convertView,R.id.comment_layout);

            goal_content.setText(contract.getGoal().getGoalContent());
            claim.setText(contract.getGoal().getClaim());
            created_at.setText(contract.getCreatedAt());
            //查找comment并填充至comment layout
            BmobQuery<CardReply>bmobQuery = new BmobQuery<>();
            Card card = new Card();
            card.setObjectId(contract.getObjectId());
            bmobQuery.addWhereEqualTo("card", new BmobPointer(card));
            bmobQuery.include("replyAuthor,replyTo");
            bmobQuery.order("-updatedAt");
            bmobQuery.findObjects(getActivity(), new FindListener<CardReply>() {
                @Override
                public void onSuccess(List<CardReply> list) {
                    int size = list.size();
                    comment_layout.removeAllViews();
                    for (int i=0;i<size;i++){
                        User replyAuthor = list.get(i).getReplyAuthor();
                        User replyTo = list.get(i).getReplyTo();
                        String comment = list.get(i).getContent();
                        String line = replyAuthor.getNick()+":"+(replyTo==null?"":("@"+replyTo.getNick()+"  "))+comment;
                        TextView commentLine = new TextView(getActivity());
                        commentLine.setText(line);
                        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        commentLine.setLayoutParams(layoutParams);
                        comment_layout.addView(commentLine);
                    }
                }
                @Override
                public void onError(int i, String s) {
                    ShowToast("查询评论失败");
                }
            });

            //设置点赞按钮
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
                        public void onSuccess(List<User> object) {//记录点赞
                            //查询是否已经点赞过
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
            //设置评论按钮
            final Button comment = ViewHolder.get(convertView,R.id.add_comment);
            comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i("click", "btn_comment clicked");
                    //显示input栏
                    layout_input_bar.setVisibility(View.VISIBLE);
                    //显示一个margin
                    //显示输入法
                    showSoftInputView();
                    //调整列表位置
                    listView.setSelection(arg0 + 1);
                    //listView.setSelectionFromTop(listView.getCount() - 1,);
                    //设置那些地方地方
                    layout_more.setVisibility(View.VISIBLE);
                    Log.i("measure", "layout_input_bar:" + layout_input_bar.getMeasuredHeight() + " " + layout_input_bar.getMeasuredState() + " " + layout_input_bar.getMeasuredHeightAndState());
                    Log.i("measure", "listView:" + listView.getMeasuredHeight() + " " + listView.getMeasuredState() + " " + listView.getMeasuredHeightAndState());
                    Log.i("measure", "layout_input_bar:" + layout_input_bar.getMeasuredHeight() + " " + layout_input_bar.getMeasuredState() + " " + layout_input_bar.getMeasuredHeightAndState());

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
                                ShowToast(R.string.network_tips + "");
                                return;
                            }
                            CardReply cardReply = new CardReply();
                            cardReply.setCard(cardList.get(arg0));
                            cardReply.setContent(msg);
                            User replyAuthor = BmobUser.getCurrentUser(getActivity(), User.class);
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