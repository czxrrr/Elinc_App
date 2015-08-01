package com.bmob.im.demo.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.bmob.im.demo.R;
import com.bmob.im.demo.bean.Answer;
import com.bmob.im.demo.bean.Question;
import com.bmob.im.demo.bean.Tool;
import com.bmob.im.demo.bean.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobRelation;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.GetListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

/**
 * Created by HUBIN on 2015/7/25.
 */
public class QuestionItemActivityElinc extends ActivityBase {
    Bundle bundle;
    ListView listView;
    List<Map<String,String>> mapList;
    SimpleAdapter questionAdapter;
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
                EditText a= (EditText) findViewById(R.id.edit_answer);
                QuestionItemActivityElinc.this.submitAnswer(a.getText().toString());
            }
        });
        initBasicView();
        initQuestionContent();
        initList();
    }
    private void initQuestionContent(){
        String id=bundle.getString("questionId");
        BmobQuery<Question> bmobQuery = new BmobQuery<Question>();
        bmobQuery.getObject(this, id, new GetListener<Question>() {
            @Override
            public void onSuccess(Question object) {
                // TODO Auto-generated method stub
                TextView titleTV=(TextView)findViewById(R.id.question_item_title);
                titleTV.setText(object.getTitle());
                TextView contentTV=(TextView)findViewById(R.id.question_item_question_content);
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
        listView = (ListView)findViewById(R.id.answer_list);
    }
    private void initList(){
        mapList = new ArrayList<Map<String, String>>();
        BmobQuery<Answer> bmobQuery = new BmobQuery<Answer>();
        //String bql ="select include question,include responder,* from Answer where questionId = pointer('Question', '"+
        //        bundle.getString("questionId")+"')";
        //bmobQuery.setSQL(bql);
        bmobQuery.addWhereEqualTo("questionId", bundle.getString("questionId"));
        bmobQuery.include("questionId,responder");
        bmobQuery.order("-createdAt");
        bmobQuery.findObjects(QuestionItemActivityElinc.this, new FindListener<Answer>() {
            @Override
            public void onSuccess(List<Answer> list) {
                if (list != null && list.size() > 0) {
                    Log.i("test", list.get(0).getAnswerContent());
                    for (int i = 0; i < list.size(); i++) {
                        Tool.alert(QuestionItemActivityElinc.this, "查询成功");
                        //Log.i("smile",list.get(i).getQustionId().getQuestionContent());
                        Map<String, String> map = new HashMap<String, String>();
                        map.put("answer_content", list.get(i).getAnswerContent());
                        map.put("responder", list.get(i).getResponder().getNick());
                        mapList.add(map);
                    }
                    questionAdapter = new SimpleAdapter(QuestionItemActivityElinc.this, mapList, R.layout.item_answer_in_list_elinc,
                            new String[]{"answer_content", "responder"},
                            new int[]{R.id.answer_content, R.id.responder
                            });
                    listView.setAdapter(questionAdapter);

                } else {
                    Log.i("smile", "查询成功，无数据返回");
                    Tool.alert(QuestionItemActivityElinc.this, "暂时没有答案，添加一个吧");
                }
            }

            @Override
            public void onError(int i, String s) {
                Tool.alert(QuestionItemActivityElinc.this,"提交失败，请检查网络");
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
                Tool.alert(QuestionItemActivityElinc.this, "提交失败，请检查网络"+arg0+arg1);
            }
        });
    }
    public void submitAnswer(String a){
        User user = BmobUser.getCurrentUser(this, User.class);
        System.out.println("123");
        Question question=new Question();
        question.setObjectId( bundle.getString("questionId"));
        System.out.println("4");
        Answer answer=new Answer();
        answer.setAnswerContent(a);
        answer.setQuestionId(question);
        answer.setResponder(user);
        System.out.println("5");
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
}
