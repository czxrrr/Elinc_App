package com.bmob.im.demo.ui;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.bmob.im.demo.R;
import com.bmob.im.demo.bean.Question;
import com.bmob.im.demo.bean.Tool;
import com.bmob.im.demo.bean.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.bmob.im.BmobUserManager;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobRelation;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

public class NewQuestionActivityElinc extends ActivityBase {
    EditText questionContent;
    EditText questionTitle;
    List<String> tags;
    EditText et_input_tags;
    BmobUserManager userManager = BmobUserManager.getInstance(NewQuestionActivityElinc.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_question_elinc);
        init();
        initTagButton();
        initListener();
    }
    private void init(){
        initTopBarForLeft("新问题");
        userManager.init(this);
        questionContent = (EditText)findViewById(R.id.question_content);
        questionTitle= (EditText) findViewById(R.id.question_title);
        et_input_tags= (EditText) findViewById(R.id.et_input_tags);
    }
    private void initTagButton(){
        Button btn_add_tag1= (Button) findViewById(R.id.btn_add_tag1);
        btn_add_tag1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String a=et_input_tags.getText().toString();
                if(a==""){a=a+",美食";}
                else{a=a+"美食";}
                et_input_tags.setText(a);
            }
        });
    }
    private void initListener(){
        Button button = (Button)findViewById(R.id.button_new_question);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String questionContentText = questionContent.getText().toString();
                String questionTitleText = questionTitle.getText().toString();

                tags= Arrays.asList((et_input_tags.getText().toString().split(",|，| |　",3)));
                AddQuestion(questionTitleText, questionContentText);
            }
        });
    }
    public void AddQuestion(String title,String questionContent){
        final Question question = new Question();
        //注意：不能调用Question.setObjectId("")方法
        question.setTags(tags);
        question.setQuestionContent(questionContent);
        question.setTitle(title);
        question.setAuthor(BmobUser.getCurrentUser(this, User.class));
        question.save(NewQuestionActivityElinc.this, new SaveListener() {
            @Override
            public void onSuccess() {
                // TODO Auto-generated method stub
                String id=question.getObjectId();
                Tool.alert(NewQuestionActivityElinc.this, "提问成功，请静候答案");
                User user = BmobUser.getCurrentUser(NewQuestionActivityElinc.this, User.class);
                BmobRelation relation = new BmobRelation();
                Question q=new Question();
                q.setObjectId(id);
                relation.add(q);
                user.setFollow(relation);
                user.update(NewQuestionActivityElinc.this, new UpdateListener() {
                    @Override
                    public void onSuccess() {
                        // TODO Auto-generated method stub
                        //Log.i("life", "多对多关联添加成功");
                        //Tool.alert(NewQuestionActivityElinc.this, "收藏成功");
                    }

                    @Override
                    public void onFailure(int arg0, String arg1) {
                        // TODO Auto-generated method stub
                        // Log.i("life", "多对多关联添加失败");
                        //Tool.alert(NewQuestionActivityElinc.this, "提交失败，请检查网络");
                    }
                });
                finish();
            }

            @Override
            public void onFailure(int code, String arg0) {
                // TODO Auto-generated method stub
                Tool.alert(NewQuestionActivityElinc.this, "提问失败，请查看网络状态");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_question, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
