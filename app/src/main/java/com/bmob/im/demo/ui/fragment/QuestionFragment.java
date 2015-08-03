package com.bmob.im.demo.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.im.task.BRequest;
import cn.bmob.im.util.BmobLog;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.CountListener;
import cn.bmob.v3.listener.FindListener;

import com.bmob.im.demo.R;
import com.bmob.im.demo.adapter.QuestionListAdapter;
import com.bmob.im.demo.bean.Question;
import com.bmob.im.demo.bean.Tool;
import com.bmob.im.demo.ui.FragmentBase;
import com.bmob.im.demo.ui.QuestionItemActivityElinc;
import com.bmob.im.demo.util.CollectionUtils;
import com.bmob.im.demo.view.xlist.XListView;
import com.bmob.im.demo.view.xlist.XListView.IXListViewListener;

/** 添加好友
 * @ClassName: SearchQuestion
 * @Description: TODO
 * @author smile
 * @date 2014-6-5 下午5:26:41
 */
public class QuestionFragment extends FragmentBase implements OnClickListener,IXListViewListener,OnItemClickListener{
    EditText et_search_question;
    Button btn_search_question;
    List<Question> question = new ArrayList<Question>();
    XListView mListView;
    QuestionListAdapter adapter;
    private View view;
    ListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_question, container, false);
        listView = (ListView)view.findViewById(R.id.question_list);
        return view;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
    }

    private void initView(){
        //initTopBarForLeft("查找问题");
        et_search_question = (EditText)findViewById(R.id.et_search_question);
        btn_search_question = (Button)findViewById(R.id.btn_search_question);
        btn_search_question.setOnClickListener(this);
        initXListView();

    }

    private void initXListView() {
        mListView = (XListView) findViewById(R.id.list_search);
        // 首先不允许加载更多
        mListView.setPullLoadEnable(false);
        // 不允许下拉
        mListView.setPullRefreshEnable(false);
        // 设置监听器
        mListView.setXListViewListener(this);
        //设置下拉刷新
        mListView.pullRefreshing();

        adapter = new QuestionListAdapter(getActivity(), question);
        mListView.setAdapter(adapter);

        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                String questionId = question.get(position-1).getObjectId();
                //ShowToast("point"+position);
                bundle.putString("questionId", questionId);
                intent.putExtras(bundle);
                intent.setClass(getActivity(), QuestionItemActivityElinc.class);
                startAnimActivity(intent);
            }
        });
        //Log.i("smile", "123=====");
    }

    int curPage = 0;
    ProgressDialog progress ;
    private void initSearchList(final boolean isUpdate){
        if(!isUpdate){
            progress = new ProgressDialog(getActivity());
            progress.setMessage("正在搜索...");
            progress.setCanceledOnTouchOutside(true);
            progress.show();
        }
        BmobQuery<Question> eq1 = new BmobQuery<Question>();
        eq1.addWhereContains("title", et_search_question.getText().toString());
        BmobQuery<Question> eq2 = new BmobQuery<Question>();
        eq2.addWhereContains("question_content", et_search_question.getText().toString());
        BmobQuery<Question> eq3=new BmobQuery<Question>();
        eq3.addWhereContains("tags", et_search_question.getText().toString());
        List<BmobQuery<Question>> queries = new ArrayList<BmobQuery<Question>>();
        queries.add(eq1);
        queries.add(eq2);
        queries.add(eq3);
        BmobQuery<Question> mainQuery = new BmobQuery<Question>();
        mainQuery.or(queries);
        mainQuery.findObjects(getActivity(), new FindListener<Question>() {
            @Override
            public void onSuccess(List<Question> list) {
                // TODO Auto-generated method stub
                if (CollectionUtils.isNotNull(list)) {
                    if (isUpdate) {
                        question.clear();
                    }
                    adapter.addAll(list);
                    if (list.size() < BRequest.QUERY_LIMIT_COUNT) {
                        mListView.setPullLoadEnable(false);
                        ShowToast("问题搜索完成!");
                    } else {
                        mListView.setPullLoadEnable(true);
                    }
                } else {
                    BmobLog.i("查询成功:无返回值");
                    if (question != null) {
                        question.clear();
                    }
                    ShowToast("没有您要找的问题，去提问吧");
                }
                if (!isUpdate) {
                    progress.dismiss();
                } else {
                    refreshPull();
                }
                //这样能保证每次查询都是从头开始
                curPage = 0;
            }
            @Override
            public void onError(int code, String msg) {
                // TODO Auto-generated method stub
                BmobLog.i("查询错误:" + msg);
                if (question != null) {
                    question.clear();
                }
                ShowToast("问题不存在");
                mListView.setPullLoadEnable(false);
                refreshPull();
                //这样能保证每次查询都是从头开始
                curPage = 0;
            }
        });

    }

    /** 查询更多
     * @Title: queryMoreNearList
     * @Description: TODO
     * @param @param page
     * @return void
     * @throws
     */
    private void queryMoreSearchList(int page){
        BmobQuery<Question> query=new BmobQuery<Question>();
        query.findObjects(getActivity(), new FindListener<Question>() {
            @Override
            public void onSuccess(List<Question> list) {
                // TODO Auto-generated method stub
                if (CollectionUtils.isNotNull(list)) {
                    adapter.addAll(list);
                }
                refreshLoad();
            }

            @Override
            public void onError(int i, String s) {
                // TODO Auto-generated method stub
                ShowLog("搜索更多问题出错:"+s);
                mListView.setPullLoadEnable(false);
                refreshLoad();
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        // TODO Auto-generated method stub
        /*BmobChatUser user = (BmobChatUser) adapter.getItem(position-1);
        Intent intent =new Intent(this,SetMyInfoActivity.class);
        intent.putExtra("from", "add");
        intent.putExtra("username", user.getUsername());
        startAnimActivity(intent);*/
    }

    String searchName ="";
    @Override
    public void onClick(View arg0) {
        // TODO Auto-generated method stub
        switch (arg0.getId()) {
            case R.id.btn_search_question://搜索
                question.clear();
                searchName = et_search_question.getText().toString();
                if(searchName!=null && !searchName.equals("")){
                    initSearchList(false);
                }else{
                    ShowToast("请输入搜索内容");
                }
                ShowToast(searchName);
                break;

            default:
                break;
        }
    }

    @Override
    public void onRefresh() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onLoadMore() {
        // TODO Auto-generated method stub
        userManager.querySearchTotalCount(searchName, new CountListener() {

            @Override
            public void onSuccess(int arg0) {
                // TODO Auto-generated method stub
                if(arg0 >question.size()){
                    curPage++;
                    queryMoreSearchList(curPage);
                }else{
                    ShowToast("数据加载完成");
                    mListView.setPullLoadEnable(false);
                    refreshLoad();
                }
            }

            @Override
            public void onFailure(int arg0, String arg1) {
                // TODO Auto-generated method stub
                ShowLog("查询附近的人总数失败"+arg1);
                refreshLoad();
            }
        });
    }

    private void refreshLoad(){
        if (mListView.getPullLoading()) {
            mListView.stopLoadMore();
        }
    }

    private void refreshPull(){
        if (mListView.getPullRefreshing()) {
            mListView.stopRefresh();
        }
    }
}








/*package com.bmob.im.demo.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.bmob.im.demo.R;
import com.bmob.im.demo.bean.Question;
import com.bmob.im.demo.bean.Tool;
import com.bmob.im.demo.ui.FragmentBase;
import com.bmob.im.demo.ui.QuestionItemActivityElinc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobQueryResult;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SQLQueryListener;

*//** 最近会话
 * @ClassName: QuestionFragment
 * @Description: TODO
 * @author smile
 * @date 2014-6-7 下午1:01:37
 *//*
public class QuestionFragment extends FragmentBase{
    private View view;
    private ListView listView;
    private SimpleAdapter questionAdapter;
    private List<Map<String,String>> mapList;
    private LinearLayout waitBarLayout;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_question, container, false);
        listView = (ListView)view.findViewById(R.id.question_list);
        return view;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initList();
    }

    private void initList(){
        mapList = new ArrayList<Map<String, String>>();
        questionAdapter = new SimpleAdapter(getActivity(), mapList, R.layout.item_question_in_list_elinc,
                new String[]{"title", "question_content", "author"},
                new int[]{R.id.title, R.id.question_content, R.id.author
                });
        listView.setAdapter(questionAdapter);
    }

    private void initItemListener(){
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                String questionId = mapList.get(position).get("objectId");
                bundle.putString("questionId", questionId);
                intent.putExtras(bundle);
                intent.setClass(getActivity(), QuestionItemActivityElinc.class);
                startAnimActivity(intent);
            }
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        refreshList();
    }
    private void refreshList(){
        waitBarLayout = (LinearLayout)findViewById(R.id.wait_bar);
        waitBarLayout.setVisibility(View.VISIBLE);
        getActivity().setProgressBarVisibility(true);
        String bql ="select include author,* from Question order by -createdAt";
        BmobQuery<Question> bmobQuery = new BmobQuery<Question>();
        bmobQuery.setSQL(bql);
        bmobQuery.doSQLQuery(getActivity(), new SQLQueryListener<Question>() {
            @Override
            public void done(BmobQueryResult<Question> result, BmobException e) {
                if (e == null) {
                    List<Question> list = (List<Question>) result.getResults();
                    if (list != null && list.size() > 0) {
                        mapList.clear();
                        for (int i = 0; i < list.size(); i++) {
                            Map<String, String> map = new HashMap<String, String>();
                            map.put("title", list.get(i).getTitle());
                            map.put("question_content", list.get(i).getQuestionContent());
                            map.put("objectId", list.get(i).getObjectId());
                            map.put("author", list.get(i).getAuthor().getNick());
                            mapList.add(map);
                        }
                        questionAdapter.notifyDataSetChanged();
                        Tool.setListViewHeightBasedOnChildren(listView);
                        initItemListener();
                        waitBarLayout.setVisibility(View.GONE);
                    } else {
                        Log.i("smile", "查询成功，无数据返回");
                    }
                } else {
                    Log.i("smile", "错误码：" + e.getErrorCode() + "，错误描述：" + e.getMessage());
                }
            }
        });
    }
}*/
