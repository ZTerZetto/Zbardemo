package com.automation.zzx.intelligent_basket_demo.activity.basket;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.activity.loginRegist.LoginActivity;
import com.automation.zzx.intelligent_basket_demo.adapter.basket.BasketPlaneAdapter;
import com.automation.zzx.intelligent_basket_demo.entity.AppConfig;
import com.automation.zzx.intelligent_basket_demo.entity.PositionInfo;
import com.automation.zzx.intelligent_basket_demo.entity.UserInfo;
import com.automation.zzx.intelligent_basket_demo.utils.ToastUtil;
import com.automation.zzx.intelligent_basket_demo.utils.ftp.FTPUtil;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseCallBack;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseOkHttpClient;
import com.automation.zzx.intelligent_basket_demo.widget.image.SmartImageView;
import com.github.chrisbanes.photoview.PhotoView;
import com.scwang.smartrefresh.header.BezierCircleHeader;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.SpinnerStyle;
import com.scwang.smartrefresh.layout.footer.BallPulseFooter;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.io.File;

import android.net.Uri;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

import static java.lang.Math.sqrt;

public class PlaneFigureActivity extends AppCompatActivity implements View.OnClickListener {

    private final static int PULL_DOWN = 102;  // 下拉刷新
    private final static int UPDATE_IMAGE = 103; // 更新图片
    private final static int NO_MORE_IMAGE = 104; // 暂未上传平面图
    private final static int UPDATE_FAILED = 105; // 更新失败
    private final static int UPDATE_SUCESS = 106; // 更新成功

    private final static int SHOW_LOCATION_FAIL = 110;//无坐标信息时
    private final static int SHOW_LOCATION = 111; //显示坐标信息

    private double DISTANCE; // 点击范围阈值
    private final static double COMPENSATION_X = 0; // 水平位置偏移量30
    private final static double COMPENSATION_Y = 0; // 竖直位置偏移量34
    private List<String> urls  = new ArrayList<>(); // bitmap 位图
    private List<Bitmap> bitmaps  = new ArrayList<>(); // 文件Url
    private PositionInfo positionInfoA= new PositionInfo("A","A",-75686.174,32863.739);
    private PositionInfo positionInfoB= new PositionInfo("B","B",45847.787,126467.042);
    private List<PositionInfo> infoList = new ArrayList<>();
    private Map<String, PositionInfo> positionMap = new HashMap<>();

    // 控件声明
    private RefreshLayout mSmartRefreshLayout;
    private SmartImageView mSmartIv;
    private LinearLayout llError;
    private TextView tvTitle;
    private ListView lvBuild;
    private BasketPlaneAdapter buildPlaneAdapter;

    //选择弹窗
    private AlertDialog mSelectProjectDialog;  // 选择角色弹窗
    private int currentSelected = 0; // 当前角色位置
    private int tmpSelected = 0; // 临时角色位置
    private String[] mBuildString ;  // 疑似点击楼号列表

    //屏幕适配
    private float dst_2[] = new float[2];

    // 用户登录信息相关
    private UserInfo mUserInfo;
    private String mProjectId;
    private String mToken;
    private SharedPreferences mPref;

    //FTP相关
    private FTPUtil mFTPClient;

    // 消息Handler
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PULL_DOWN:  // 下拉刷新
                    //displayWorkPhoto(1, mFileNameList.get(0));
                    break;
                case SHOW_LOCATION:  // 更新图片
                    //更新适配器
                    buildPlaneAdapter.notifyDataSetChanged();
                    break;

                case SHOW_LOCATION_FAIL:
                    //隐藏适配器
                    lvBuild.setVisibility(View.GONE);
                    tvTitle.setVisibility(View.GONE);

                case NO_MORE_IMAGE: // 尚无更多的图片
                    mSmartRefreshLayout.finishRefresh(500); // 刷新动画结束
                    mSmartRefreshLayout.finishLoadMore(500); // 加载动画结束
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plane_figure);
        // 初始化控件
        initWidgetResource();

        // 初始化FTP连接
        initFTPClient();

        // 初始化用户信息
        getUserInfo();

        //初始化坐标点信息
        //initPosition();

        //网络获取总平面图及坐标信息
        Intent intent = this.getIntent();
        mProjectId = intent.getStringExtra("projectId");
        if(mProjectId != null){
            getPlaneFigureWithInfo();
        }
    }

    private void getPlaneFigureWithInfo() {
        //从FTP上获取总平面图
        String root_url = AppConfig.FILE_SERVER_YBLIU_PATH + "project" + File.separator +
                mProjectId + File.separator + "plane_graph_all.jpg";
        mSmartIv.setImageUrl(root_url, R.mipmap.ic_empty);

        // 获取相关图片

        // displayPlanePhoto(0, mRemotePath,cert_name);

        //从后台获取平面图信息
        BaseOkHttpClient.newBuilder()
                .addHeader("Authorization", mToken)
                .addParam("projectId", mProjectId)
                .addParam("buildingNum", 0)
                .addParam("type", 1) //请求整个项目的平面图信息
                .get()
                .url(AppConfig.GET_PLANE_GRAPH_INFO)
                .build()
                .enqueue(new BaseCallBack() {
                    @Override
                    public void onSuccess(Object o) {
                        String response = o.toString();
                        JSONObject jsonObject = JSON.parseObject(response);
                        Boolean isLogin = jsonObject.getBoolean("isLogin");
                        if(isLogin){
                            JSONArray data = jsonObject.getJSONArray("planeGraphInfo");
                            parseGraghInfo(data);
                        }
                    }

                    @Override
                    public void onError(int code) {
                        switch(code){
                            case 401:
                                ToastUtil.showToastTips(PlaneFigureActivity.this, "登陆已过期，请重新登录");
                                startActivity(new Intent(PlaneFigureActivity.this, LoginActivity.class));
                                PlaneFigureActivity.this.finish();
                                break;
                            case 403:
                                break;
                        }
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {

                    }
                });

    }

    private void parseGraghInfo(JSONArray data){
        infoList.clear();
        if(data.size() != 0){
            for(int index=0; index <data.size();index++) {
                JSONObject basketObj = (JSONObject)data.get(index);
                String buildId = basketObj.getString("building_id");
                if(buildId.equals("A")){
                    //分隔符得到XY坐标
                    String location =  basketObj.getString("location");
                    String[] locations = location.split(",");
                    double location_x = Double.valueOf(locations[0]);
                    double location_y = Double.valueOf(locations[1]);
                    positionInfoA = new PositionInfo(buildId, "", location_x,location_y);
                }else if(buildId.equals("B")){
                    //分隔符得到XY坐标
                    String location =  basketObj.getString("location");
                    String[] locations = location.split(",");
                    double location_x = Double.valueOf(locations[0]);
                    double location_y = Double.valueOf(locations[1]);
                    positionInfoB = new PositionInfo(buildId, "", location_x,location_y);
                }else{
                    //分隔符得到XY坐标
                    //空坐标判断
                    String location =  basketObj.getString("location");
                    String basketList = basketObj.getString("deviceList");
                    double location_x;
                    double location_y;
                    if(location == null || location.isEmpty()){
                        location_x = 0;
                        location_y = 0;
                        llError.setVisibility(View.VISIBLE);
                    } else {
                        String[] locations = location.split(",");
                        location_x = Double.valueOf(locations[0]);
                        location_y = Double.valueOf(locations[1]);
                    }
                    PositionInfo positionInfo = new PositionInfo(buildId, basketList, location_x,location_y);
                    infoList.add(positionInfo);
                }
            }
            for(int i = 0; i < infoList.size();i++){
                positionMap.put(infoList.get(i).getId(),infoList.get(i));
            }
                mHandler.sendEmptyMessage(SHOW_LOCATION);
        }else{
            mHandler.sendEmptyMessage(SHOW_LOCATION_FAIL);
        }
    }




   /* private void initPosition(){
        PositionInfo positionInfo1= new PositionInfo("1","1,2",-30053.379,63739.285);
        infoList.add(positionInfo1);
        PositionInfo positionInfo2= new PositionInfo("7","3,4,5",-30053.379,43249.445);
        infoList.add(positionInfo2);
        PositionInfo positionInfo3= new PositionInfo("8","6,7",-208.307,65478.735);
        infoList.add(positionInfo3);

        for(int i = 0; i < infoList.size();i++){
            positionMap.put(infoList.get(i).getId(),infoList.get(i));
        }
    }
*/
    /*
     * 控件初始化
     */
    private void initWidgetResource() {
        // 上拉、下拉刷新
        mSmartRefreshLayout = (SmartRefreshLayout) findViewById(R.id.smart_refresh_layout);
        mSmartRefreshLayout.setRefreshHeader(  //设置 Header 为 贝塞尔雷达 样式
                new BezierCircleHeader(this));
        mSmartRefreshLayout.setRefreshFooter(  //设置 Footer 为 球脉冲 样式
                new BallPulseFooter(this).setSpinnerStyle(SpinnerStyle.Scale));
        mSmartRefreshLayout.setPrimaryColorsId(R.color.smart_loading_background_color);
        mSmartRefreshLayout.setOnRefreshListener(new OnRefreshListener() { // 添加下拉刷新监听
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                if(urls.size() > 0)
                    mHandler.sendEmptyMessage(PULL_DOWN);
            }
        });

        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("总平面图");
        titleText.setText("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        mSmartIv = findViewById(R.id.general_layout);
        mSmartIv.setOnTouchListener(onTouchListener);
        llError = findViewById(R.id.ll_error);
        tvTitle = findViewById(R.id.tv_title);
        lvBuild = findViewById(R.id.lv_build);
        buildPlaneAdapter = new BasketPlaneAdapter(this,R.layout.item_area_plane,infoList);
        lvBuild.setAdapter(buildPlaneAdapter);


        //消息响应
        lvBuild.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(PlaneFigureActivity.this, infoList.get(position).getId() + "号楼",
                        Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(PlaneFigureActivity.this, PlaneBasketActivity.class);
                intent.putExtra("build_id", infoList.get(position).getId());
                intent.putExtra("project_id", mProjectId);
                startActivity(intent);
            }
        });
    }


    // FTP初始化
    private void initFTPClient(){
        mFTPClient = new FTPUtil(AppConfig.FILE_SERVER_YBLIU_IP, AppConfig.FILE_SERVER_YBLIU_PORT,
                AppConfig.FILE_SERVER_USERNAME, AppConfig.FILE_SERVER_PASSWORD);
    }

    /*
     * 解析用户信息
     */
    // 获取用户数据
    private void getUserInfo(){
        // 从本地获取数据
        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        mUserInfo = new UserInfo();
        mUserInfo.setUserId(mPref.getString("userId", ""));
        mUserInfo.setUserPhone(mPref.getString("userPhone", ""));
        mUserInfo.setUserRole(mPref.getString("userRole", ""));
        mToken = mPref.getString("loginToken","");
        mProjectId = mPref.getString("projectId","");
    }

   /* // 获取要显示图片的url
    private void displayPlanePhoto(final int direction, final String url,final String filename){
        new Thread(){
            public void run(){
                try{
                    mFTPClient.openConnect();  // 建立连接
                    mFTPClient.downloadingInit(url+filename);  // 切换工作环境
                    List<String> newFileNames = mFTPClient.getDownloadFileName(direction, filename);
                    if(newFileNames.size() == 0){  // 没有更多的图片
                        mHandler.sendEmptyMessage(NO_MORE_IMAGE);
                    }else {   // 图片更新
                        Message msg = new Message();  // 通知页面更新
                        msg.what = UPDATE_IMAGE;
                        msg.arg1 = direction;
                        msg.obj = newFileNames;
                        mHandler.sendMessage(msg);
                    }
                    mFTPClient.closeConnect();  // 关闭连接
                    mHandler.sendEmptyMessage(DISPLAY_PLANE);
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }.start();
    }
*/
    /*
    * 点击事件
    * */
    @Override
    public void onClick(View v) {


    }

    // 顶部导航栏消息响应
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home: // 返回按钮
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //事件监听方法
    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    //转换控件边界
                    //Matrix matrix = new Matrix();
                    //Matrix inverse = mSmartIv.getImageMatrix();
                    Matrix matrix = new Matrix(mSmartIv.getImageMatrix());
                    Matrix inverse = new Matrix();
                    matrix.invert(inverse);
                    inverse.mapPoints(dst_2, new float[]{mSmartIv.getWidth(), mSmartIv.getHeight()});
                    DISTANCE = 0.05*(dst_2[0]+dst_2[1]);

                    List<PositionInfo> positionInfos = areaJudge(v, event);
                    float x = event.getX();
                    float y = event.getY();
                    // 目标点的坐标
                    float dst[] = new float[2];
                    // 获取到ImageView的matrix
                    Matrix imageMatrix = mSmartIv.getImageMatrix();
                    // 创建一个逆矩阵
                    Matrix inverseMatrix = new Matrix();

                    // 求逆，逆矩阵被赋值
                    imageMatrix.invert(inverseMatrix);
                    // 通过逆矩阵映射得到目标点 dst 的值
                    inverseMatrix.mapPoints(dst, new float[]{x, y});

                    if(positionInfos != null) {
                        if(positionInfos.size() == 1) {
                            Toast.makeText(PlaneFigureActivity.this, positionInfos.get(0).getId() + "号楼",
                                    Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(PlaneFigureActivity.this, PlaneBasketActivity.class);
                            intent.putExtra("build_id", positionInfos.get(0).getId());
                            intent.putExtra("project_id", mProjectId);
                            startActivity(intent);

                        } else if(positionInfos.size() > 1) {
                            mBuildString =  new String[positionInfos.size()];
                            for(int i=0;i<positionInfos.size();i++){
                                mBuildString[i] = positionInfos.get(i).getId();
                            }
                            showSelectDialog();

                        }

                    }
                    break;
                default:
                    break;
            }
            return false;
        }
    };

    private List<PositionInfo> areaJudge(View v, MotionEvent event){
        List<PositionInfo> positionInfos = new ArrayList<>();
        float x = event.getX();
        float y = event.getY();
        // 目标点的坐标
        float dst[] = new float[2];
        // 获取到ImageView的matrix
        Matrix imageMatrix = mSmartIv.getImageMatrix();
        // 创建一个逆矩阵
        Matrix inverseMatrix = new Matrix();
        // 求逆，逆矩阵被赋值
        imageMatrix.invert(inverseMatrix);
        // 通过逆矩阵映射得到目标点 dst 的值
        inverseMatrix.mapPoints(dst, new float[]{x, y});
        // 判断dstX, dstY在Bitmap上的位置即可
        for(int i=0;i<infoList.size();i++){
            double distance_x = dst[0]-parseToScreen(infoList.get(i)).getPosition_X();
            double distance_y = dst[1]-parseToScreen(infoList.get(i)).getPosition_Y();
            double distance = sqrt(distance_x*distance_x+distance_y*distance_y);
            if(distance < DISTANCE) positionInfos.add(infoList.get(i));
        }
        return positionInfos;
    }

    //坐标转换至空间像素
    private PositionInfo parseToScreen(PositionInfo positionInfo){
        PositionInfo mPosition;
        Double x = (positionInfo.getPosition_X()-positionInfoA.getPosition_X())
                    /(positionInfoB.getPosition_X()-positionInfoA.getPosition_X());
        Double y = (positionInfo.getPosition_Y()-positionInfoB.getPosition_Y())
                /(positionInfoA.getPosition_Y()-positionInfoB.getPosition_Y());
        mPosition = new PositionInfo(positionInfo.getId(),positionInfo.getItemId(),
                dst_2[0]*x+COMPENSATION_X,
                dst_2[1]*y+COMPENSATION_Y);
        return mPosition;
    }


    // 弹出身份选择框
    public void showSelectDialog(){
        final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("选择楼号");
        alertBuilder.setSingleChoiceItems(mBuildString, currentSelected, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int position) {
                tmpSelected = position;
            }
        });

        alertBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                currentSelected = tmpSelected;
                //跳转至所选择的楼号主界面
                Toast.makeText(PlaneFigureActivity.this, mBuildString[currentSelected] + "号楼",
                            Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(PlaneFigureActivity.this, PlaneBasketActivity.class);
                intent.putExtra("build_id", mBuildString[currentSelected]);
                intent.putExtra("project_id", mProjectId);
                startActivity(intent);
                mSelectProjectDialog.dismiss();
            }
        });

        alertBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mSelectProjectDialog.dismiss();
            }
        });

        mSelectProjectDialog = alertBuilder.create();
        mSelectProjectDialog.show();
    }


}
