package com.bmob.im.demo.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.bmob.im.demo.R;
import com.bmob.im.demo.bean.Card;
import com.bmob.im.demo.bean.Goal;
import com.bmob.im.demo.bean.Tool;
import com.bmob.im.demo.bean.User;
import com.bmob.im.demo.ui.FragmentBase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobRelation;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;

public class GoalFragment extends FragmentBase {
    private View view;
    private List<Map<String,String>>mapList;
    private MyAdapter myAdapter;
    private ListView listView;
    Map<String,String> map;
     public GoalFragment() {
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
        view = inflater.inflate(R.layout.fragment_goal, container, false);
        initList();
        return view;
    }
    private void initList(){
        mapList = new ArrayList<>();
        listView = (ListView)view.findViewById(R.id.goal_list);
        myAdapter = new MyAdapter(getActivity(), mapList, R.layout.item_card_in_list_elinc,
                new String[]{"goal_content", "claim", "day", "created_at"},
                new int[]{R.id.goal_content, R.id.claim, R.id.day, R.id.created_at});
        listView.setAdapter(myAdapter);
    }
    private void refreshList(){
        BmobQuery<Card>query = new BmobQuery<>();
        query.findObjects(getActivity(), new FindListener<Card>() {
            @Override
            public void onSuccess(List<Card> list) {
                mapList.clear();
                for (int i=0;i<list.size();i++){
                    map = new HashMap<>();
                    map.put("goal_content",list.get(i).getGoalContent());
                    map.put("claim",list.get(i).getClaim());
                    map.put("day",list.get(i).getClaim());
                    map.put("created_at",list.get(i).getUpdatedAt());
                    mapList.add(map);
                }
                myAdapter.notifyDataSetChanged();
                Tool.setListViewHeightBasedOnChildren(listView);
            }
            @Override
            public void onError(int i, String s) {
                Toast.makeText(getActivity(),"打卡记录获取失败",Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshList();
    }

    private class MyAdapter extends SimpleAdapter {
        public MyAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            final Button add_good = (Button)view.findViewById(R.id.add_good);
            final Button add_comment = (Button)view.findViewById(R.id.add_comment);
            add_good.setTag(position);
            add_comment.setTag(position);
            add_good.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setLike(position);
                }
            });
            add_comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Toast.makeText(getActivity(),"给"+position+"评论",Toast.LENGTH_SHORT).show();
                }
            });
            return view;
        }
    }
    private void setLike(Integer position){
        User user = BmobUser.getCurrentUser(getActivity(), User.class);
        String id;
        Toast.makeText(getActivity(), "给" + position + "点赞", Toast.LENGTH_SHORT).show();
        id = mapList.get(position).get("objectId");

        Card card = new Card();
        card.setObjectId(id);
        BmobRelation relation = new BmobRelation();
        relation.add(user);
        card.setlikedBy(relation);
        card.update(getActivity(), new UpdateListener() {
            @Override
            public void onSuccess() {
                // TODO Auto-generated method stub
                            /*Log.i("life", "多对多关联添加成功");*/
                Tool.alert(getActivity(), "点赞成功");
            }

            @Override
            public void onFailure(int arg0, String arg1) {
                // TODO Auto-generated method stub
                            /*Log.i("life", "多对多关联添加失败");*/
                Tool.alert(getActivity(), "提交失败，请检查网络");
            }
        });
    }
}
