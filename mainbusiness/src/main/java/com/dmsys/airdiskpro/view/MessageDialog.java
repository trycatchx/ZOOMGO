package com.dmsys.airdiskpro.view;

import android.content.Context;
import android.graphics.Paint;
import android.support.annotation.ColorRes;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.dmsys.mainbusiness.R;


public class MessageDialog extends UDiskBaseDialog {

    public MessageDialog(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        super.initView(TYPE_TWO_BTN, R.layout.dialog_message);
        this.setCancelable(true);
    }

    public MessageDialog(Context context, int type) {
        super(context);
        // TODO Auto-generated constructor stub
        super.initView(type, R.layout.dialog_message);
        this.setCancelable(true);
    }

    public void setMessage(CharSequence text) {
        View customView = this.getCustomView();
        TextView msgView = (TextView) customView.findViewById(R.id.dialog_msg);
        msgView.setText(text);
    }

    public void setSubContent(String text) {
        ((TextView) getCustomView().findViewById(R.id.tv_sub_content)).setMovementMethod(new ScrollingMovementMethod());
        ((TextView) getCustomView().findViewById(R.id.tv_sub_content)).setText(text);
        getCustomView().findViewById(R.id.tv_sub_content).setVisibility(View.VISIBLE);
    }

    public void setSubContentColor(@ColorRes int color) {

        ((TextView) getCustomView().findViewById(R.id.tv_sub_content)).setTextColor(getContext().getResources().getColor(color));
    }

    public void setSubContentUnderLine() {
        ((TextView) getCustomView().findViewById(R.id.tv_sub_content)).getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); // 下划线
        ((TextView) getCustomView().findViewById(R.id.tv_sub_content)).getPaint().setAntiAlias(true);
    }

    public void setMessageListener(View.OnClickListener l) {
        getCustomView().findViewById(R.id.tv_sub_content).setOnClickListener(l);
    }


}
