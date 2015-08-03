package com.bmob.im.demo.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bmob.im.demo.R;
import com.bmob.im.demo.adapter.base.BaseListAdapter;
import com.bmob.im.demo.adapter.base.ViewHolder;
import com.bmob.im.demo.bean.Card;
import com.bmob.im.demo.bean.User;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.datatype.BmobRelation;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.GetListener;
import cn.bmob.v3.listener.UpdateListener;

/**
 * Created by HUBIN on 2015/8/3.
 */
public class CardListAdapter extends BaseListAdapter<Card> {

    public CardListAdapter(Context context, List<Card> list) {
        super(context, list);
    }

    @Override
    public View bindView(int arg0, View convertView, ViewGroup arg2) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_card_in_list_elinc, null);
        }
        final Card contract = getList().get(arg0);
        final User me = BmobUser.getCurrentUser(mContext, User.class);
        TextView goal_content = ViewHolder.get(convertView, R.id.goal_content);
        TextView claim = ViewHolder.get(convertView, R.id.claim);

        goal_content.setText(contract.getGoal().getGoalContent());
        claim.setText(contract.getGoal().getClaim());
//        created_at.setText(contract.getCreatedAt());
//        day.setText(contract.getGoal().getDay());

        final Button like = ViewHolder.get(convertView,R.id.add_like);
        Button comment = ViewHolder.get(convertView,R.id.add_comment);
        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BmobQuery<User>query = new BmobQuery<>();
                Card card = new Card();
                card.setObjectId(contract.getObjectId());
                query.addWhereRelatedTo("likedBy", new BmobPointer(card));
                query.findObjects(mContext, new FindListener<User>() {
                    @Override
                    public void onSuccess(List<User> object) {
                        Log.i("life", "查询个数：" + object.size());
                        boolean like = false;
                        for (int i=0;i<list.size();i++){
                            if (object.get(i).getObjectId().equals(me.getObjectId())){
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
        comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        return convertView;
    }

}
