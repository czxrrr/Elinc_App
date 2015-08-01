package com.bmob.im.demo.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.bmob.im.demo.R;
import com.bmob.im.demo.bean.Question;
import com.bmob.im.demo.bean.Tool;
import com.bmob.im.demo.bean.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.bmob.im.BmobUserManager;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.datatype.BmobRelation;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;

public class MyFavoriteActivityElinc extends ActivityBase {
    private ListView listView;
    private SimpleAdapter questionAdapter;
    private List<Map<String,String>> mapList;
    private List<Question> myFavourite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_favorite_activity_elinc);
        initTopBarForLeft("问题收藏");
        listView = (ListView)findViewById(R.id.question_list);
        initListView();
        initItemListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_my_favorite_activity_elinc, menu);
        return true;
    }

    private void initListView(){
        mapList = new ArrayList<>();
        BmobQuery<Question> query = new BmobQuery<>();
        User u = BmobUser.getCurrentUser(this, User.class);
        u.setObjectId(u.getObjectId());
        query.include("author");
        query.addWhereRelatedTo("follow", new BmobPointer(u));
        query.findObjects(this, new FindListener<Question>() {
            @Override
            public void onSuccess(List<Question> list) {
                Log.i("life", "查询个数：" + list.size());
                myFavourite=list;
                for (int i = 0; i < list.size(); i++) {
                    Map<String, String> map = new HashMap<>();
                    map.put("title", list.get(i).getTitle());
                    map.put("question_content", list.get(i).getQuestionContent());
                    map.put("objectId", list.get(i).getObjectId());
                    map.put("author", list.get(i).getAuthor().getUsername());
                    mapList.add(map);
                }
                questionAdapter = new SimpleAdapter(MyFavoriteActivityElinc.this, mapList, R.layout.item_question_in_list_elinc,
                        new String[]{"title", "question_content", "author"},
                        new int[]{R.id.title, R.id.question_content, R.id.author
                        });
                listView.setAdapter(questionAdapter);
                Tool.setListViewHeightBasedOnChildren(listView);
            }

            @Override
            public void onError(int code, String msg) {
                Log.i("life", "查询失败：" + code + "-" + msg);
            }
        });
    }

    private void initItemListener(){
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MyFavoriteActivityElinc.this, "" + position, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                String questionId = mapList.get(position).get("objectId");
                bundle.putString("questionId", questionId);
                intent.putExtras(bundle);
                intent.setClass(MyFavoriteActivityElinc.this, QuestionItemActivityElinc.class);
                startAnimActivity(intent);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                dialog(myFavourite.get(position));
                return false;
            }
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        //initListView();
        initItemListener();
    }
    protected void dialog(final Question question) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MyFavoriteActivityElinc.this);
        builder.setMessage("确认取消关注吗？");
        builder.setTitle("取消关注");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                User user = BmobUser.getCurrentUser(MyFavoriteActivityElinc.this, User.class);
                BmobRelation relation = new BmobRelation();
                relation.remove(question);
                user.setFollow(relation);
                user.update(MyFavoriteActivityElinc.this, new UpdateListener() {
                    @Override
                    public void onSuccess() {
                        // TODO Auto-generated method stub
                        Log.i("life", "关联关系删除成功");
                        Tool.alert(MyFavoriteActivityElinc.this,"取消关注成功");
                    }

                    @Override
                    public void onFailure(int arg0, String arg1) {
                        // TODO Auto-generated method stub
                        Log.i("life", "关联关系删除失败：" + arg0 + "-" + arg1);
                        Tool.alert(MyFavoriteActivityElinc.this, "取消关注失败，请检查网络");
                    }
                });
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
           @Override
           public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
           }
        });
        builder.create().show();
    }
}
