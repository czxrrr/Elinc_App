package com.bmob.im.demo.adapter;

import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import cn.bmob.im.BmobChatManager;
import cn.bmob.im.config.BmobConfig;
import cn.bmob.push.a.name;
import cn.bmob.v3.listener.PushListener;

import com.bmob.im.demo.R;
import com.bmob.im.demo.adapter.base.BaseListAdapter;
import com.bmob.im.demo.adapter.base.ViewHolder;
import com.bmob.im.demo.bean.Question;
import com.bmob.im.demo.util.ImageLoadOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class QuestionListAdapter extends BaseListAdapter<Question> {

    public QuestionListAdapter(Context context, List<Question> list) {
        super(context, list);
        // TODO Auto-generated constructor stub
    }

    @Override
    public View bindView(int arg0, View convertView, ViewGroup arg2) {
        // TODO Auto-generated method stub
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_question_in_list_elinc, null);
        }
        final Question contract = getList().get(arg0);
        TextView title = ViewHolder.get(convertView, R.id.title);
        TextView question_content = ViewHolder.get(convertView, R.id.question_content);

        //Button btn_add = ViewHolder.get(convertView, R.id.question_content);

        //String avatar = contract.getAvatar();

        /*if (avatar != null && !avatar.equals("")) {
            ImageLoader.getInstance().displayImage(avatar, iv_avatar, ImageLoadOptions.getOptions());
        } else {
            iv_avatar.setImageResource(R.drawable.default_head);
        }*/

        title.setText(contract.getTitle());
        /*btn_add.setText("");
        btn_add.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                final ProgressDialog progress = new ProgressDialog(mContext);
                progress.setMessage("�������...");
                progress.setCanceledOnTouchOutside(false);
                progress.show();
                //����tag����
                BmobChatManager.getInstance(mContext).sendTagMessage(BmobConfig.TAG_ADD_CONTACT, contract.getObjectId(),new PushListener() {

                    @Override
                    public void onSuccess() {
                        // TODO Auto-generated method stub
                        progress.dismiss();
                        ShowToast("��������ɹ����ȴ�Է���֤!");
                    }

                    @Override
                    public void onFailure(int arg0, final String arg1) {
                        // TODO Auto-generated method stub
                        progress.dismiss();
                        ShowToast("��������ʧ�ܣ����������!");
                        ShowLog("��������ʧ��:"+arg1);
                    }
                });
            }
        });*/
        return convertView;
    }

}
