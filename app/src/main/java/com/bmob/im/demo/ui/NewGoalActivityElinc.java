package com.bmob.im.demo.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;

import com.bmob.im.demo.R;
import com.bmob.im.demo.bean.Goal;
import com.bmob.im.demo.bean.Tool;
import com.bmob.im.demo.bean.User;

import java.util.List;

import cn.bmob.im.BmobUserManager;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;

public class NewGoalActivityElinc extends ActivityBase {
    private Integer numberOfGoal;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_goal_activity_elinc);
        initTopBarForLeft("设置新目标");
        pre_process();
        NumberPicker numberPicker = (NumberPicker)findViewById(R.id.day);
        numberPicker.setMinValue(10);
        numberPicker.setMaxValue(100);
        Button button = (Button)findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(numberOfGoal<3) {
                    BmobUserManager bmobUserManager = BmobUserManager.getInstance(NewGoalActivityElinc.this);
                    User me = bmobUserManager.getCurrentUser(User.class);
                    Goal goal = new Goal();
                    EditText goal_content = (EditText) findViewById(R.id.goal_content);
                    EditText claim = (EditText) findViewById(R.id.claim);
                    NumberPicker numberPicker = (NumberPicker) findViewById(R.id.day);
                    goal.setGoalContent(goal_content.getText().toString());
                    goal.setClaim(claim.getText().toString());
                    goal.setDay(numberPicker.getValue());
                    goal.setAuthor(me);
                    goal.save(NewGoalActivityElinc.this, new SaveListener() {
                        @Override
                        public void onSuccess() {
                            Tool.alert(NewGoalActivityElinc.this, "save success");
                        }

                        @Override
                        public void onFailure(int i, String s) {

                        }
                    });
                    Log.i("1", goal_content.getText().toString());
                    Log.i("1", claim.getText().toString());
                    Log.i("1", numberPicker.getValue() + "");
                }else{
                    Tool.alert(NewGoalActivityElinc.this,"亲，最多只能设立3个目标哦！(*^__^*)！");
                }

                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_aim_activity_elinc, menu);
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
    public void pre_process(){
        User u = BmobUser.getCurrentUser(NewGoalActivityElinc.this, User.class);
        BmobQuery<Goal> query = new BmobQuery<>();
        //用此方式可以构造一个BmobPointer对象。只需要设置objectId就行
        User user = new User();
        user.setObjectId(u.getObjectId());
        query.addWhereEqualTo("author", new BmobPointer(user));
        query.addWhereNotEqualTo("out",true);
        query.findObjects(this, new FindListener<Goal>() {
            @Override
            public void onSuccess(List<Goal> object) {
                numberOfGoal = object.size();
                Tool.alert(NewGoalActivityElinc.this,""+object.size());
            }

            @Override
            public void onError(int code, String msg) {
            }
        });

    }
}
