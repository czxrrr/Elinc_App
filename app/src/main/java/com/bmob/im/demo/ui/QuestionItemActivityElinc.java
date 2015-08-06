package com.bmob.im.demo.ui;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.bmob.im.demo.R;
import com.bmob.im.demo.adapter.AddFriendAdapter;
import com.bmob.im.demo.adapter.AnswerListAdapter;
import com.bmob.im.demo.adapter.QuestionListAdapter;
import com.bmob.im.demo.bean.Answer;
import com.bmob.im.demo.bean.Question;
import com.bmob.im.demo.bean.Tool;
import com.bmob.im.demo.bean.User;
import com.bmob.im.demo.util.CollectionUtils;
import com.bmob.im.demo.view.xlist.XListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.im.task.BRequest;
import cn.bmob.im.util.BmobLog;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobRelation;
import cn.bmob.v3.listener.CountListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.GetListener;
import cn.bmob.v3.listener.QuerySMSStateListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

/**
 * Created by HUBIN on 2015/7/25.
 */
public class QuestionItemActivityElinc extends ActivityBase  implements View.OnClickListener,XListView.IXListViewListener,AdapterView.OnItemClickListener {
    Bundle bundle;
    List<Map<String,String>> mapList;
    SimpleAdapter questionAdapter;
    List<Answer> answer = new ArrayList<Answer>();
    XListView mListView;
    AnswerListAdapter adapter;
    private View view;
    ListView listView;
    int curPage = 0;
    ProgressDialog progress ;

    private void initXListView() {
        mListView = (XListView) findViewById(R.id.list_answer_e);
        // 首先不允许加载更多
        mListView.setPullLoadEnable(true);
        // 不允许下拉
        mListView.setPullRefreshEnable(true);
        // 设置监听器
        mListView.setXListViewListener(this);
        //
        mListView.pullRefreshing();

        adapter = new AnswerListAdapter(this, answer);
        mListView.setAdapter(adapter);

        mListView.setOnItemClickListener(this);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_detail_elinc);
        bundle = getIntent().getExtras();
        /*点击按钮收藏*/
        final Button follow= (Button) findViewById(R.id.follow);
        follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                QuestionItemActivityElinc.this.followQustion();
            }
        });
        /*点击按钮 提交答案*/
        final Button submit= (Button) findViewById(R.id.submit_answer);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText a = (EditText) findViewById(R.id.edit_answer);
                QuestionItemActivityElinc.this.submitAnswer(a.getText().toString());
            }
        });
        initBasicView();
        initQuestionContent();
        initList();
        refreshList();
    }
    private void initQuestionContent(){
        String id=bundle.getString("questionId");
        BmobQuery<Question> bmobQuery = new BmobQuery<Question>();
        bmobQuery.getObject(this, id, new GetListener<Question>() {
            @Override
            public void onSuccess(Question object) {
                // TODO Auto-generated method stub
                TextView titleTV = (TextView) findViewById(R.id.question_item_title);
                titleTV.setText(object.getTitle());
                TextView contentTV = (TextView) findViewById(R.id.question_item_question_content);
                contentTV.setText(object.getQuestionContent());
                //Tool.alert(QuestionItemActivityElinc.this,"查询成功");
            }

            @Override
            public void onFailure(int code, String msg) {
                // TODO Auto-generated method stub
                Tool.alert(QuestionItemActivityElinc.this, "查询失败：" + msg);
            }
        });
    }
    private void initBasicView() {
        initTopBarForLeft("校园问答");
    }
    private void initList(){
        initXListView();
        BmobQuery<Answer> bmobQuery = new BmobQuery<Answer>();
        bmobQuery.addWhereEqualTo("questionId", bundle.getString("questionId"));
        bmobQuery.include("responder,questionId");
        bmobQuery.order("-createdAt");
        bmobQuery.findObjects(QuestionItemActivityElinc.this, new FindListener<Answer>() {
            @Override
            public void onSuccess(List<Answer> list) {

                    // TODO Auto-generated method stub
                    if (CollectionUtils.isNotNull(list)) {
                        answer.clear();

                        adapter.addAll(list);
                        if (list.size() < BRequest.QUERY_LIMIT_COUNT) {
                            mListView.setPullLoadEnable(false);
                            ShowToast("问题搜索完成!");
                        } else {
                            mListView.setPullLoadEnable(true);
                        }
                    } else {
                        BmobLog.i("查询成功:无返回值");
                        if (answer != null) {
                            answer.clear();
                        }
                        ShowToast("没有您要找的问题，去提问吧");
                    }
                    if (!true) {
                        progress.dismiss();
                    } else {
                        refreshPull();
                    }
                    //这样能保证每次查询都是从头开始
                    curPage = 0;
            }

            @Override
            public void onError(int i, String s) {
                Tool.alert(QuestionItemActivityElinc.this, "提交失败，请检查网络");
            }
        });
    }

    @Override
    public void onLoadMore() {
        // TODO Auto-generated method stub
        BmobQuery<Answer> bmobQuery = new BmobQuery<Answer>();
        bmobQuery.addWhereEqualTo("questionId", bundle.getString("questionId"));
        bmobQuery.include("questionId,responder");
        bmobQuery.order("-createdAt");
        bmobQuery.findObjects(QuestionItemActivityElinc.this, new FindListener<Answer>() {
            @Override
            public void onSuccess(List<Answer> list) {
                // TODO Auto-generated method stub
                if (list.size() > answer.size()) {
                    curPage++;
                    queryMoreSearchList(curPage);
                } else {
                    ShowToast("数据加载完成");
                    mListView.setPullLoadEnable(false);
                    refreshLoad();
                }
            }

            @Override
            public void onError(int i, String s) {
                ShowLog("查询附近的人总数失败" + s);
                refreshLoad();
            }
        });

    }

    private void queryMoreSearchList(int page){
        BmobQuery<Answer> bmobQuery = new BmobQuery<Answer>();
        bmobQuery.addWhereEqualTo("questionId", bundle.getString("questionId"));
        bmobQuery.include("questionId,responder");
        bmobQuery.order("-createdAt");
        bmobQuery.findObjects(QuestionItemActivityElinc.this, new FindListener<Answer>() {
            @Override
            public void onSuccess(List<Answer> arg0) {
                // TODO Auto-generated method stub
                if (CollectionUtils.isNotNull(arg0)) {
                    answer.clear();
                    adapter.addAll(arg0);
                }
                refreshLoad();
            }

            @Override
            public void onError(int arg0, String arg1) {
                // TODO Auto-generated method stub
                ShowLog("搜索更多用户出错:" + arg1);
                mListView.setPullLoadEnable(false);
                refreshLoad();
            }

        });
    }



    public void followQustion(){
        /*此方法不需要传参数，只能被QustionItemActivity的关注按钮的毁掉函数调用
        * 会自动从bundle 里面获取本question的Id 进行关注
        * 从而用户的关注里面会多一个问题
        *
        *                           czr
        * */
        Question question=new Question();
        question.setObjectId( bundle.getString("questionId"));
        User user = BmobUser.getCurrentUser(this, User.class);
        BmobRelation relation = new BmobRelation();
        relation.add(question);
        user.setFollow(relation);
        user.update(this, new UpdateListener() {
            @Override
            public void onSuccess() {
                // TODO Auto-generated method stub
                Log.i("life", "多对多关联添加成功");
                Tool.alert(QuestionItemActivityElinc.this, "收藏成功");
            }

            @Override
            public void onFailure(int arg0, String arg1) {
                // TODO Auto-generated method stub
                Log.i("life", "多对多关联添加失败");
                Tool.alert(QuestionItemActivityElinc.this, "提交失败，请检查网络" + arg0 + arg1);
            }
        });
    }
    public void submitAnswer(String a){
        User user = BmobUser.getCurrentUser(this, User.class);
        Question question=new Question();
        question.setObjectId( bundle.getString("questionId"));
        System.out.println("4");
        Answer answer=new Answer();
        answer.setAnswerContent(a);
        answer.setQuestionId(question);
        answer.setResponder(user);
        Tool.alert(QuestionItemActivityElinc.this, a);
        answer.save(QuestionItemActivityElinc.this, new SaveListener() {
            @Override
            public void onSuccess() {
                System.out.println("yes");
                Tool.alert(QuestionItemActivityElinc.this, "提交成功");
                finish();
            }

            @Override
            public void onFailure(int i, String s) {
                Tool.alert(QuestionItemActivityElinc.this, "提交失败，请检查网络");
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
    public void onRefresh() {
        refreshList();

    }

    public void refreshList(){
        BmobQuery<Answer> bmobQuery = new BmobQuery<Answer>();
        bmobQuery.addWhereEqualTo("questionId", bundle.getString("questionId"));
        bmobQuery.include("questionId,responder");
        bmobQuery.order("-createdAt");
        bmobQuery.findObjects(QuestionItemActivityElinc.this, new FindListener<Answer>() {
            @Override
            public void onSuccess(List<Answer> arg0) {
                // TODO Auto-generated method stub
                if (CollectionUtils.isNotNull(arg0)) {
                    answer.clear();
                    adapter.addAll(arg0);
                    mListView.stopRefresh();
                }
                refreshLoad();
            }

            @Override
            public void onError(int arg0, String arg1) {
                // TODO Auto-generated method stub
                ShowLog("搜索更多用户出错:" + arg1);
                mListView.setPullLoadEnable(false);
                mListView.stopRefresh();
                refreshLoad();

            }

        });
    }
    @Override
    public void onClick(View v) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
}
