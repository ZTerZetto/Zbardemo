package com.automation.zzx.intelligent_basket_demo.adapter.common;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.entity.MessageInfo;

import java.util.List;

/**
 * Created by pengchenghu on 2019/4/8.
 * Author Email: 15651851181@163.com
 * Describe:
 */
public class UserMessageAdapter extends RecyclerView.Adapter<UserMessageAdapter.ViewHolder>{

    private Context mContext;
    private List<MessageInfo> mMessageInfoList;
    private OnItemClickListener mOnItemClickListener;  // 消息监听

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        View mView;
        TextView tvTime;//消息时间

        LinearLayout llOrdinaryMessage;//普通消息
        TextView tvTitle;//消息标题
        ImageView ivChecked; // 消息未读提示
        TextView tvContent;//消息内容

        LinearLayout llAlarmMessage;//报警消息
        TextView tvAlarmTitle;//消息标题
        ImageView ivAlarmChecked; // 消息未读提示
        TextView tvAlarmType;// 报警类型
        TextView tvBasketId;// 吊篮编号
        TextView tvSiteNo;// 现场编号
        TextView tvProjectName;// 项目名称

        private OnItemClickListener onItemClickListener;

        public ViewHolder(View itemView, final UserMessageAdapter.OnItemClickListener onItemClickListener) {
            super(itemView);
            // 控件初始化
            mView = itemView;
            tvTime = itemView.findViewById(R.id.tv_message_time);
            //普通消息
            llOrdinaryMessage = itemView.findViewById(R.id.ll_ordinary_message);
            tvTitle = itemView.findViewById(R.id.tv_message_title);
            ivChecked = itemView.findViewById(R.id.iv_message_no_read);
            tvContent = itemView.findViewById(R.id.tv_message_description);
            //报警消息
            llAlarmMessage = itemView.findViewById(R.id.ll_alarm_message);
            tvAlarmTitle = itemView.findViewById(R.id.tv_alarm_message_title);
            ivAlarmChecked = itemView.findViewById(R.id.iv_alarm_message_no_read);
            tvAlarmType = itemView.findViewById(R.id.tv_alarm_message_1);
            tvBasketId = itemView.findViewById(R.id.tv_alarm_message_2);
            tvSiteNo = itemView.findViewById(R.id.tv_alarm_message_3);
            tvProjectName = itemView.findViewById(R.id.tv_alarm_message_4);

            // 消息监听
            this.onItemClickListener = onItemClickListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            // getpostion()为Viewholder自带的一个方法，用来获取RecyclerView当前的位置，将此作为参数，传出去
            onItemClickListener.onItemClick(v, getAdapterPosition());
        }
    }

    public UserMessageAdapter(Context mContext, List<MessageInfo> messageInfoList){
        this.mContext = mContext;
        mMessageInfoList = messageInfoList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_message_info,viewGroup,false);
        final ViewHolder holder = new ViewHolder(view, mOnItemClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        MessageInfo mMessageInfo = mMessageInfoList.get(i);
        viewHolder.tvTime.setText(mMessageInfo.getmTime());
        //类型判断 报警消息：type=1    普通消息：type=2/3
        switch (mMessageInfo.getmType()) {
            case "1":  // 报警消息
                //隐藏普通消息，显示报警消息
                viewHolder.llOrdinaryMessage.setVisibility(View.GONE);
                viewHolder.llAlarmMessage.setVisibility(View.VISIBLE);
                //数据写入
                viewHolder.tvAlarmTitle.setText(mMessageInfo.getmTitle());
                viewHolder.tvAlarmType.setText(mMessageInfo.getmAlarmType());
                viewHolder.tvBasketId.setText(mMessageInfo.getmBasketId());
                viewHolder.tvSiteNo.setText(mMessageInfo.getmSiteNo());
                viewHolder.tvProjectName.setText(mMessageInfo.getmProjectName());
                if (mMessageInfo.ismIsChecked()) viewHolder.ivAlarmChecked.setVisibility(View.GONE);
                else viewHolder.ivAlarmChecked.setVisibility(View.VISIBLE);
                break;
            case "2": // 验收申请
            case "3": // 项目流程
            case "4": // 报修信息
            case "5": // 配置清单
                //隐藏普通消息，显示报警消息
                viewHolder.llOrdinaryMessage.setVisibility(View.VISIBLE);
                viewHolder.llAlarmMessage.setVisibility(View.GONE);
                //数据写入
                viewHolder.tvTitle.setText(mMessageInfo.getmTitle());
                viewHolder.tvContent.setText(mMessageInfo.getmDescription());
                if (mMessageInfo.ismIsChecked()) viewHolder.ivChecked.setVisibility(View.GONE);
                else viewHolder.ivChecked.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mMessageInfoList.size();
    }

    /*
     * 设置监听
     */
    public void setOnItemClickListener(UserMessageAdapter.OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    /*
     * 点击接口函数
     */
    public interface OnItemClickListener {
        /**
         * 当RecyclerView某个被点击的时候回调
         * @param view 点击item的视图
         * @param position 点击得到位置
         */
        public void onItemClick(View view, int position);
    }
}
