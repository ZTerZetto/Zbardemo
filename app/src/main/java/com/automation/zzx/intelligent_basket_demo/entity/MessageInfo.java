package com.automation.zzx.intelligent_basket_demo.entity;

import android.os.Parcel;
import android.os.Parcelable;

import org.litepal.crud.DataSupport;

/**
 * Describe: 推送消息类
 */

public class MessageInfo extends DataSupport implements Parcelable {

    private String mTime;  // 时间
    private String mTitle; // 消息标题
    private String mDescription; // 消息内容

    // 消息类型
    private String mType; // 消息类型-> 1：报警消息 2.验收申请类 3.项目流程类
    private String mAlarmType;// 报警消息类型 -> 1：图像报警 2：参数报警

    // 项目相关
    private String mProjectId;  // 项目编号
    private String mProjectName; // 项目名称

    // 相关人员
    private String mWorkerList;  // 施工人员列表

    public String getmWorkerName() {
        return mWorkerName;
    }

    public void setmWorkerName(String mWorkerName) {
        this.mWorkerName = mWorkerName;
    }

    private String mWorkerName;  //施工人员名称
    private String mWorkerPhone; // 施工人员联系方式
    private String mRentAdminPhone; // 租方管理员联系方式

    // ID 类
    private String mAreaAdminId;
    private String mRentAdminId;
    private String mProjectAdaminId;

    // 吊篮相关
    private String mBasketId; // 设备Id
    private String mSiteNo; // 现场编号 2020.09.07

    //报警描述类
    private String url; // 识别图片链接

    private boolean mIsChecked; // 是否查看过该信息

    /**
     *  构造函数
     */
    public MessageInfo() {

    }

    public MessageInfo(String mTime, String mTitle, String mDescription) {
        this.mTime = mTime;
        this.mTitle = mTitle;
        this.mDescription = mDescription;
        this.mIsChecked = false;
    }

    /*
     * Bean 函数
     */
    public String getmTime() {
        return mTime;
    }

    public void setmTime(String mTime) {
        this.mTime = mTime;
    }

    public String getmTitle() {
        return mTitle;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getmDescription() {
        return mDescription;
    }

    public void setmDescription(String mDescription) {
        this.mDescription = mDescription;
    }

    public String getmType() {
        return mType;
    }

    public void setmType(String mType) {
        this.mType = mType;
    }

    public String getmAlarmType() {
        return mAlarmType;
    }

    public void setmAlarmType(String mAlarmType) {
        this.mAlarmType = mAlarmType;
    }

    public String getmProjectId() {
        return mProjectId;
    }

    public void setmProjectId(String mProjectId) {
        this.mProjectId = mProjectId;
    }

    public String getmProjectName() {
        return mProjectName;
    }

    public void setmProjectName(String mProjectName) {
        this.mProjectName = mProjectName;
    }

    public String getmWorkerList(){
        return mWorkerList;
    }

    public void setmWorkerList(String mWorkerList) {
        this.mWorkerList = mWorkerList;
    }

    public String getmWorkerPhone() {
        return mWorkerPhone;
    }

    public void setmWorkerPhone(String mWorkerPhone) {
        this.mWorkerPhone = mWorkerPhone;
    }

    public String getmRentAdminPhone() {
        return mRentAdminPhone;
    }

    public void setmRentAdminPhone(String mRentAdminPhone) {
        this.mRentAdminPhone = mRentAdminPhone;
    }

    public String getmAreaAdminId(){
        return mAreaAdminId;
    }

    public void setmAreaAdminId(String mAreaAdminId){
        this.mAreaAdminId = mAreaAdminId;
    }

    public String getmRentAdminId(){
        return mRentAdminId;
    }

    public void setmRentAdminId(String mRentAdminId){
        this.mRentAdminId = mRentAdminId;
    }

    public String getmProjectAdaminId(){
        return mProjectAdaminId;
    }

    public void setmProjectAdaminId(String mProjectAdaminId){
        this.mProjectAdaminId = mProjectAdaminId;
    }

    public String getmBasketId() {
        return mBasketId;
    }

    public void setmBasketId(String mBasketId) {
        this.mBasketId = mBasketId;
    }

    public boolean ismIsChecked() {
        return mIsChecked;
    }

    public void setmIsChecked(boolean mIsChecked) {
        this.mIsChecked = mIsChecked;
    }

    public String getmSiteNo() {
        return mSiteNo;
    }

    public void setmSiteNo(String siteNo) {
        this.mSiteNo = siteNo;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    /*
     * Parcel 序列化
     */
    protected MessageInfo(Parcel in) {
        mTime = in.readString();
        mTitle = in.readString();
        mDescription = in.readString();
        mType = in.readString();
        mAlarmType = in.readString();//2020.09.07
        mProjectId = in.readString();
        mProjectName = in.readString();
        mWorkerList = in.readString();
        mWorkerName = in.readString();
        mWorkerPhone = in.readString();
        mRentAdminPhone = in.readString();
        mBasketId = in.readString();
        mSiteNo = in.readString(); //2020.09.07
        url = in.readString();
        mIsChecked = in.readByte() != 0;
    }

    public static final Creator<MessageInfo> CREATOR = new Creator<MessageInfo>() {
        @Override
        public MessageInfo createFromParcel(Parcel in) {
            return new MessageInfo(in);
        }

        @Override
        public MessageInfo[] newArray(int size) {
            return new MessageInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mTime);
        dest.writeString(mTitle);
        dest.writeString(mDescription);
        dest.writeString(mType);
        dest.writeString(mAlarmType);
        dest.writeString(mProjectId);
        dest.writeString(mProjectName);
        dest.writeString(mWorkerList);
        dest.writeString(mWorkerName);
        dest.writeString(mWorkerPhone);
        dest.writeString(mRentAdminPhone);
        dest.writeString(mBasketId);
        dest.writeString(mSiteNo);
        dest.writeString(url);
        dest.writeByte((byte) (mIsChecked ? 1 : 0));
    }
}

