package com.example.zzx.zbar_demo.entity;

/**
 * Created by pengchenghu on 2019/3/25.
 * Author Email: 15651851181@163.com
 * Describe: 租方管理员管理吊篮
 */
public class MgBasketInfo {
    //private boolean selected;  // 是否被选中
    private String indexImageUri; // 吊篮首页图片
    private String id;  // 吊篮Id
    private String state; // 吊篮运行状态
    private String outStorage; // 吊篮出库日期
    private String principal; // 主要负责人

    /*
     * 构造函数
     */
    public MgBasketInfo(){
        //this.selected = false;
    }
    public MgBasketInfo(String indexImageUri, String id, String state, String outStorage,
                        String principal){
        //this.selected = false;
        this.indexImageUri =indexImageUri;
        this.id = id;
        this.state = state;
        this.outStorage = outStorage;
        this.principal = principal;
    }

    /*
     * Bean函数
     */

//    public boolean isSelected() {
//        return selected;
//    }
//
//    public void setSelected(boolean selected) {
//        this.selected = selected;
//    }

    public String getIndexImageUri() {
        return indexImageUri;
    }

    public void setIndexImageUri(String indexImageUri) {
        this.indexImageUri = indexImageUri;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getOutStorage() {
        return outStorage;
    }

    public void setOutStorage(String outStorage) {
        this.outStorage = outStorage;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }
}
