package com.automation.zzx.intelligent_basket_demo.utils.xiaomi.mipush;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;

import com.automation.zzx.intelligent_basket_demo.R;

/**
 * Created by pengchenghu on 2019/3/19.
 * Author Email: 15651851181@163.com
 * Describe: 用于设置推送时间-开始时间+暂停时间 继承OnTimeChangedListener接口
 *
 */
public class TimeIntervalDialog extends Dialog implements TimePicker.OnTimeChangedListener {

    private TimeIntervalInterface mTimeIntervalInterface;
    private Context mContext;
    private TimePicker mStartTimePicker, mEndTimePicker;
    private int mStartHour, mStartMinute, mEndHour, mEndMinute;

    private Button.OnClickListener clickListener = new Button.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.apply:
                    dismiss();
                    //设置时间参数
                    mTimeIntervalInterface.apply(mStartHour, mStartMinute, mEndHour, mEndMinute);
                    break;
                case R.id.cancel:
                    dismiss();
                    mTimeIntervalInterface.cancel();
                    break;
                default:
                    break;
            }
        }
    };

    public TimeIntervalDialog(Context context, TimeIntervalInterface timeIntervalInterface,
                              int startHour, int startMinute, int endHour, int endMinute) {
        super(context);
        mContext = context;
        this.mTimeIntervalInterface = timeIntervalInterface;
        this.mStartHour = startHour;
        this.mStartMinute = startMinute;
        this.mEndHour = endHour;
        this.mEndMinute = endMinute;
    }

    public TimeIntervalDialog(Context context, TimeIntervalInterface timeIntervalInterface) {
        this(context, timeIntervalInterface, 0, 0, 23, 59);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_set_time);
        setCancelable(true);
        setTitle(mContext.getString(R.string.set_accept_time));
        mStartTimePicker = (TimePicker) findViewById(R.id.startTimePicker);
        mStartTimePicker.setIs24HourView(true);
        mStartTimePicker.setCurrentHour(mStartHour);
        mStartTimePicker.setCurrentMinute(mStartMinute);
        mStartTimePicker.setOnTimeChangedListener(this);
        mEndTimePicker = (TimePicker) findViewById(R.id.endTimePicker);
        mEndTimePicker.setIs24HourView(true);
        mEndTimePicker.setCurrentHour(mEndHour);
        mEndTimePicker.setCurrentMinute(mEndMinute);
        mEndTimePicker.setOnTimeChangedListener(this);
        Button applyBtn = (Button) findViewById(R.id.apply);
        applyBtn.setOnClickListener(clickListener);
        Button cancelBtn = (Button) findViewById(R.id.cancel);
        cancelBtn.setOnClickListener(clickListener);
    }

    @Override
    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
        if (view == mStartTimePicker) {
            mStartHour = hourOfDay;
            mStartMinute = minute;
        } else if (view == mEndTimePicker) {
            mEndHour = hourOfDay;
            mEndMinute = minute;
        }
    }

    interface TimeIntervalInterface {
        void apply(int startHour, int startMin, int endHour, int endMin);

        void cancel();
    }


}
