package com.bmob.im.demo.ui;

import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.bmob.im.demo.R;
import com.bmob.im.demo.adapter.base.ViewHolder;
import com.bmob.im.demo.bean.Card;
import com.bmob.im.demo.bean.Goal;
import com.bmob.im.demo.bean.Tool;
import com.bmob.im.demo.bean.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cn.bmob.im.BmobUserManager;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;

public class MyGoalActivityElinc extends ActivityBase {
    private ListView listView;
    private Goal[] goal;
    private int goalNum;
    private MyAdapter myAdapter;
    private User me;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_goal_activity_elinc);
        initTopBarForLeft("我的目标");
        me=BmobUserManager.getInstance(MyGoalActivityElinc.this).getCurrentUser(User.class);
        initList();
    }
    private void initList(){
        listView = (ListView)findViewById(R.id.my_goal_list);
        goal = new Goal[3];
        BmobUserManager bmobUserManager = BmobUserManager.getInstance(MyGoalActivityElinc.this);
        User me = bmobUserManager.getCurrentUser(User.class);
        BmobQuery<Goal> query = new BmobQuery<>();
        //用此方式可以构造一个BmobPointer对象。只需要设置objectId就行
        User user = new User();
        user.setObjectId(me.getObjectId());
        query.addWhereEqualTo("author", new BmobPointer(user));
        query.addWhereNotEqualTo("out", true);
        query.findObjects(this, new FindListener<Goal>() {
            @Override
            public void onSuccess(List<Goal> object) {
                goalNum = object.size();
                Log.i("1",goalNum+"");
                List<Map<String, String>> mapList = new ArrayList<>();
                Map<String,String>map;
                for (int i = 0; i < goalNum && i < 3; i++) {
                    goal[i] = object.get(i);
                    map = new HashMap<String, String>();
                    map.put("goal_content",goal[i].getGoalContent());
                    map.put("claim", goal[i].getClaim());
                    map.put("day", goal[i].getDay() + "");
                    map.put("created_at",goal[i].getCreatedAt());
                    mapList.add(map);
                }
                myAdapter = new MyAdapter(MyGoalActivityElinc.this, mapList, R.layout.item_my_goal_in_list_elinc,
                        new String[]{"goal_content", "claim", "day", "created_at"},
                        new int[]{R.id.goal_content, R.id.claim, R.id.day, R.id.created_at});
                listView.setAdapter(myAdapter);
                Tool.setListViewHeightBasedOnChildren(listView);
            }
            @Override
            public void onError(int code, String msg) {
                Toast.makeText(MyGoalActivityElinc.this,"无法获取我的目标！",Toast.LENGTH_SHORT).show();
            }
        });
    }


    private class MyAdapter extends SimpleAdapter{
        public MyAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            final Button button = (Button)view.findViewById(R.id.card);
            button.setTag(position);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Toast.makeText(MyGoalActivityElinc.this,"click:"+button.getTag(),Toast.LENGTH_SHORT).show();
                    Card card = new Card();
                    card.setClaim(goal[position].getClaim());
                    card.setGoalContent(goal[position].getGoalContent());
                    card.setCardsender(me);
                    card.save(MyGoalActivityElinc.this, new SaveListener() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(MyGoalActivityElinc.this,"打卡成功",Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onFailure(int i, String s) {
                            Toast.makeText(MyGoalActivityElinc.this,"网络不好..郁小林给跪了 ╮(╯﹏╰）╭",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
            return view;
        }
    };
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my_goal_activity_elinc, menu);
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
