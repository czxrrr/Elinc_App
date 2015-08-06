package com.bmob.im.demo.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bmob.im.demo.R;
import com.bmob.im.demo.adapter.base.BaseListAdapter;
import com.bmob.im.demo.adapter.base.ViewHolder;
import com.bmob.im.demo.bean.Answer;
import com.bmob.im.demo.bean.Question;
import com.bmob.im.demo.util.ImageLoadOptions;
import com.bmob.im.demo.view.CircleImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.CountListener;

/**
 * Created by czr on 2015/8/3.
 */
public class AnswerListAdapter extends BaseListAdapter<Answer> {
    Context context;
    private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
    private static class AnimateFirstDisplayListener extends SimpleImageLoadingListener {

        static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (loadedImage != null) {
                ImageView imageView = (ImageView) view;
                boolean firstDisplay = !displayedImages.contains(imageUri);
                if (firstDisplay) {
                    FadeInBitmapDisplayer.animate(imageView, 500);
                    displayedImages.add(imageUri);
                }
            }
        }
    }
    public AnswerListAdapter(Context context, List<Answer> list) {
        super(context, list);
        this.context=context;
        // TODO Auto-generated constructor stub
    }

    @Override
    public View bindView(int arg0, View convertView, ViewGroup arg2) {
        // TODO Auto-generated method stub
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_answer_in_list_elinc, null);
        }
        final Answer contract = getList().get(arg0);
        TextView answer_content = ViewHolder.get(convertView, R.id.answer_content);
        TextView answer_responder = ViewHolder.get(convertView, R.id.answer_responder);
        CircleImageView responder_avatar= ViewHolder.get(convertView,R.id.avatar_for_answer);
        answer_content.setText("" + contract.getAnswerContent());
        answer_responder.setText("    " + contract.getResponder().getUsername());
        String avatar=contract.getResponder().getAvatar();
        if(avatar!=null && !avatar.equals("")){//加载头像-为了不每次都加载头像
            ImageLoader.getInstance().displayImage(avatar, responder_avatar, ImageLoadOptions.getOptions(),animateFirstListener);
        }else {
            responder_avatar.setImageResource(R.drawable.head);
        }
        /*BmobQuery<Answer> query = new BmobQuery<Answer>();
        query.addWhereEqualTo("questionId", contract);
        query.count(context, Answer.class, new CountListener() {
            @Override
            public void onSuccess(int count) {
                // TODO Auto-generated method stub
                //toast("Barbie has played" + count + "games");
                question_number_of_answer.setText( "   hot:" + count);
            }

            @Override
            public void onFailure(int code, String msg) {
                // TODO Auto-generated method stub
                question_number_of_answer.setText("   hot:0");
            }
        });

    }*/
        return convertView;
    }
}
