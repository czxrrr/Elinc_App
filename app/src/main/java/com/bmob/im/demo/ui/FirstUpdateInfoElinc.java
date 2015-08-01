package com.bmob.im.demo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.bmob.im.demo.R;
import com.bmob.im.demo.bean.User;
import com.bmob.im.demo.config.BmobConstants;

import cn.bmob.v3.BmobInstallation;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

public class FirstUpdateInfoElinc extends BaseActivity {
    private EditText et_nick,et_uni,et_campus,et_password,et_password_again;
    private RadioGroup sex;
    private RadioButton sex_woman,sex_man;
    private Boolean chosenSex;
    private Button confirm;
    private String mobilePhone;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_update_info_elinc);
        mobilePhone = getIntent().getExtras().getString("mobile_phone");//电话号码
        Log.i("1",mobilePhone+"");
        et_password = (EditText)findViewById(R.id.et_password);
        et_password_again = (EditText)findViewById(R.id.et_password_again);
        et_campus= (EditText) findViewById(R.id.et_campus);
        et_nick= (EditText) findViewById(R.id.et_nick);
        et_uni= (EditText) findViewById(R.id.et_uni);
        sex= (RadioGroup) findViewById(R.id.sex);
        sex_woman= (RadioButton) findViewById(R.id.sex_woman);
        sex_man= (RadioButton) findViewById(R.id.sex_man);
        sex_man.setChecked(true);
        sex.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == sex_woman.getId()) {
                    chosenSex=false;
                } else if (checkedId == sex_man.getId()) {
                    chosenSex=true;
                }
            }
        });
        confirm= (Button) findViewById(R.id.confirm_first_info);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String password = et_password.getText().toString();
                if (password.equals("")) {
                    ShowToast("密码为空真的好吗");
                    return;
                }
                if (!password.equals(et_password_again.getText().toString())) {
                    ShowToast("第二个密码填错了亲");
                    return;
                }
                if (et_nick.getText().toString().equals("")) {
                    ShowToast("昵称不能不甜！");
                    return;
                }
                if (et_campus.getText().toString().equals("")) {
                    ShowToast("学校不能不恬！");
                    return;
                }
                if(et_campus.getText().toString().equals("")){
                    ShowToast("校区得填呐亲！");
                    return;
                }
                User u = new User();
                u.setPassword(password);
                u.setMobilePhoneNumber(mobilePhone);
                u.setMobilePhoneNumberVerified(true);
                u.setNick(et_nick.getText().toString());
                u.setUsername(et_nick.getText().toString());
                u.setSex(chosenSex);
                u.setCampus(et_campus.getText().toString());
                u.setUniversity(et_uni.getText().toString());
                u.setDeviceType("android");
                u.setInstallId(BmobInstallation.getInstallationId(FirstUpdateInfoElinc.this));
                u.signUp(FirstUpdateInfoElinc.this, new SaveListener() {
                    @Override
                    public void onSuccess() {
                        final User user = userManager.getCurrentUser(User.class);
                        ShowToast("注册成功");
                        // 将设备与username进行绑定
                        userManager.bindInstallationForRegister(user.getUsername());
                        //更新地理位置信息
                        updateUserLocation();
                        //发广播通知登陆页面退出
                        sendBroadcast(new Intent(BmobConstants.ACTION_REGISTER_SUCCESS_FINISH));
                        Intent intent = new Intent(FirstUpdateInfoElinc.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onFailure(int arg0, String arg1) {
                        ShowToast("用户名已被注册或者无网络访问o(╯□╰)o");
                    }
                });
            }
        });
    }
}
