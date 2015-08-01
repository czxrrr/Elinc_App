package com.bmob.im.demo.ui.fragment;

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

/** 最近会话
 * @ClassName: QuestionFragment
 * @Description: TODO
 * @author smile
 * @date 2014-6-7 下午1:01:37
 */
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
}
