package com.baidu.idl.face.main.activity.user;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.idl.face.main.activity.BaseActivity;

import com.baidu.idl.face.main.activity.batchimport.BatchImportActivity;
import com.baidu.idl.face.main.activity.register.FaceRegisterNewActivity;
import com.baidu.idl.face.main.activity.register.FaceRegisterNewDepthActivity;
import com.baidu.idl.face.main.activity.register.FaceRegisterNewNIRActivity;
import com.baidu.idl.face.main.api.FaceApi;
import com.baidu.idl.face.main.listener.OnItemClickListener;
import com.baidu.idl.face.main.listener.OnRemoveListener;
import com.baidu.idl.face.main.manager.ShareManager;
import com.baidu.idl.face.main.manager.UserInfoManager;
import com.baidu.idl.face.main.model.SingleBaseConfig;
import com.baidu.idl.face.main.model.User;
import com.baidu.idl.face.main.utils.BitmapUtils;
import com.baidu.idl.face.main.utils.DensityUtils;
import com.baidu.idl.face.main.utils.FileUtils;
import com.baidu.idl.face.main.utils.KeyboardsUtils;
import com.baidu.idl.face.main.utils.ToastUtils;
import com.baidu.idl.face.main.view.CircleImageView;
import com.baidu.idl.face.main.view.TipDialog;
import com.baidu.idl.face.main.R;

import java.util.List;

/**
 * 用户管理页面
 * Created by liujialu on 2020/02/10.
 */

public class UserManagerActivity extends BaseActivity implements View.OnClickListener,
        CompoundButton.OnCheckedChangeListener, OnItemClickListener,
        TipDialog.OnTipDialogClickListener, OnRemoveListener {
    private static final String TAG = UserManagerActivity.class.getName();

    // view
    private RecyclerView mRecyclerUserManager;
    private ImageView mImageIconSearch;        // title中的搜索按钮
    private RelativeLayout mRelativeStandard; // title中的常规布局
    private LinearLayout mLinearSearch;       // title中的搜索布局
    private ImageView mImageMenu;             // title中的菜单
    private TextView mTextCancel;             // title中的取消按钮
    private RelativeLayout mRelativeEmpty;   // 暂无内容
    private TextView mTextEmpty;
    private EditText mEditTitleSearch;

    private PopupWindow mPopupMenu;
    private RelativeLayout mRelativeDelete;   // 删除布局
    private CheckBox mCheckAll;               // 全选
    private TextView mTextDelete;             // 删除
    private TipDialog mTipDialog;

    // pop
    private RelativeLayout mPopRelativeImport;

    private Context mContext;
    private UserListListener mUserListListener;
    private FaceUserAdapter mFaceUserAdapter;
    private List<User> mUserInfoList;
    private int mSelectCount;                // 选中的个数
    private boolean mIsLongClick;           // 是否是长按
    private int mLiveType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_manager);
        mContext = this;
        initView();
        initData();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        // 读取数据库，获取用户信息
        UserInfoManager.getInstance().getUserListInfo(null, mUserListListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 数据变化，更新内存
        FaceApi.getInstance().initDatabases(true);
    }

    private void initView() {
        mRecyclerUserManager = findViewById(R.id.recycler_user_manager);
        mRecyclerUserManager.setOnClickListener(this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext);
        mRecyclerUserManager.setLayoutManager(layoutManager);
        // title相关的控件
        mRelativeStandard = findViewById(R.id.relative_standard);
        mLinearSearch = findViewById(R.id.linear_title_search);
        mImageIconSearch = findViewById(R.id.image_icon_research);
        mImageIconSearch.setOnClickListener(this);
        mImageMenu = findViewById(R.id.image_menu);
        mImageMenu.setOnClickListener(this);
        mTextCancel = findViewById(R.id.text_cancel);
        mTextCancel.setOnClickListener(this);

        mRelativeEmpty = findViewById(R.id.relative_empty);
        mTextEmpty = findViewById(R.id.text_empty);
        // 删除相关的控件
        mRelativeDelete = findViewById(R.id.relative_botton_delete);
        mRelativeDelete.setOnClickListener(this);
        mCheckAll = findViewById(R.id.check_all);
        mCheckAll.setOnCheckedChangeListener(this);
        mTextDelete = findViewById(R.id.text_delete);
        mTextDelete.setOnClickListener(this);
        // title中的搜索框取消按钮
        Button btnTitleCancel = findViewById(R.id.btn_title_cancel);
        btnTitleCancel.setOnClickListener(this);
        ImageView imageBack = findViewById(R.id.image_back);
        imageBack.setOnClickListener(this);
        ImageView imageInputDelete = findViewById(R.id.image_input_delete);
        imageInputDelete.setOnClickListener(this);
        // 初始化PopupWindow
        initPopupWindow();
        mTipDialog = new TipDialog(mContext);
        mTipDialog.setOnTipDialogClickListener(this);

        mFaceUserAdapter = new FaceUserAdapter();
        mRecyclerUserManager.setAdapter(mFaceUserAdapter);
        mFaceUserAdapter.setItemClickListener(this);
        mFaceUserAdapter.setOnRemoveListener(this);

        // title中的搜索框
        mEditTitleSearch = findViewById(R.id.edit_title_search);
        // 搜索框输入监听事件
        mEditTitleSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mUserListListener != null && s != null) {
                    // 读取数据库，获取用户信息
                    UserInfoManager.getInstance().getUserListInfo(s.toString(), mUserListListener);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    /**
     * 初始化PopupWindow
     */
    private void initPopupWindow() {
        View contentView = LayoutInflater.from(mContext).inflate(R.layout.popup_menu, null);
        mPopupMenu = new PopupWindow(contentView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        mPopupMenu.setFocusable(true);
        mPopupMenu.setOutsideTouchable(true);
        mPopupMenu.setBackgroundDrawable(getResources().getDrawable(R.drawable.menu_round));
        RelativeLayout relativeRegister = contentView.findViewById(R.id.relative_register);
        mPopRelativeImport = contentView.findViewById(R.id.relative_import);
        RelativeLayout relativeDelete = contentView.findViewById(R.id.relative_delete);
        relativeRegister.setOnClickListener(this);
        mPopRelativeImport.setOnClickListener(this);
        relativeDelete.setOnClickListener(this);
        mPopupMenu.setContentView(contentView);
    }

    private void initData() {
        mLiveType = SingleBaseConfig.getBaseConfig().getType();
        mUserListListener = new UserListListener();
        // 读取数据库，获取用户信息
        UserInfoManager.getInstance().getUserListInfo(null, mUserListListener);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_title_cancel:  // 取消按钮
                mEditTitleSearch.setText("");
                mLinearSearch.setVisibility(View.GONE);
                mRelativeStandard.setVisibility(View.VISIBLE);
                UserInfoManager.getInstance().getUserListInfo(null, mUserListListener);
                break;

            case R.id.image_icon_research:  // 搜索按钮
                mLinearSearch.setVisibility(View.VISIBLE);
                mRelativeStandard.setVisibility(View.GONE);
                break;

            case R.id.image_back:          // 返回
                finish();
                break;

            case R.id.image_menu:
                showPopupWindow(mImageMenu);
                break;

            case R.id.relative_register:      // 进入注册页面
                dismissPopupWindow();
                judgeLiveType(mLiveType, FaceRegisterNewActivity.class, FaceRegisterNewNIRActivity.class,
                        FaceRegisterNewDepthActivity.class);
                break;

            case R.id.relative_import:        // 进入批量导入页面
                dismissPopupWindow();
                Intent intent2 = new Intent(mContext, BatchImportActivity.class);
                startActivity(intent2);
                break;

            case R.id.relative_delete:       // 显示删除UI
                dismissPopupWindow();
                updateDeleteUI(true);
                break;

            case R.id.text_cancel:           // 批量删除取消
                updateDeleteUI(false);
                break;

            case R.id.text_delete:           // 批量删除
                if (mSelectCount == 0) {
                    ToastUtils.toast(getApplicationContext(), "请选择要删除的用户");
                    break;
                }
                mTipDialog.show();
                mTipDialog.setTextTitle("确认删除");
                mTipDialog.setTextMessage("删除后人脸数据不可恢复，确认删除？");
                mTipDialog.setTextConfirm("删除(" + mSelectCount + ")");
                mTipDialog.setCancelable(false);
                break;

            case R.id.image_input_delete:
                mEditTitleSearch.setText("");
                break;

            case R.id.recycler_user_manager:
                KeyboardsUtils.hintKeyBoards(v);
                break;

            case R.id.relative_botton_delete:
                Log.e(TAG, "relative_botton_delete");
                break;

            default:
                break;
        }
    }

    /**
     * 全选多选框的监听事件
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {      // 全选
            mSelectCount = mUserInfoList.size();
            for (int i = 0; i < mUserInfoList.size(); i++) {
                mUserInfoList.get(i).setChecked(true);
                mTextDelete.setText("删除(" + mSelectCount + ")");
                mTextDelete.setTextColor(Color.parseColor("#F34B56"));
            }
        } else {             // 取消全选
            mSelectCount = 0;
            for (int i = 0; i < mUserInfoList.size(); i++) {
                mUserInfoList.get(i).setChecked(false);
                mTextDelete.setText("删除(" + mSelectCount + ")");
                mTextDelete.setTextColor(Color.parseColor("#666666"));
            }
        }
        mFaceUserAdapter.notifyDataSetChanged();
    }

    // 用于adapter的item点击事件
    @Override
    public void onItemClick(View view, int position) {
        if (mRelativeDelete.getVisibility() != View.VISIBLE) {
            return;
        }
        // 如果当前item未选中，则选中
        if (!mUserInfoList.get(position).isChecked()) {
            mUserInfoList.get(position).setChecked(true);
            mSelectCount++;
            mTextDelete.setText("删除(" + mSelectCount + ")");
            mTextDelete.setTextColor(Color.parseColor("#F34B56"));
        } else {
            // 如果当前item已经选中，则取消选中
            mUserInfoList.get(position).setChecked(false);
            mSelectCount--;
            mTextDelete.setText("删除(" + mSelectCount + ")");
            if (mSelectCount == 0) {
                mTextDelete.setTextColor(Color.parseColor("#666666"));
            }
        }
        mFaceUserAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRemove(int position) {
        mUserInfoList.get(position).setChecked(true);
        mSelectCount = 1;
        mIsLongClick = true;
        mTipDialog.show();
        mTipDialog.setTextTitle("确认删除");
        mTipDialog.setTextMessage("删除后人脸数据不可恢复，确认删除？");
        mTipDialog.setTextConfirm("删除");
        mTipDialog.setCancelable(false);
    }

    // 对话框“取消”按钮
    @Override
    public void onCancel() {
        if (mIsLongClick) {
            resetDeleteData();
            mIsLongClick = false;
        }
        mFaceUserAdapter.notifyDataSetChanged();
        if (mTipDialog != null) {
            mTipDialog.dismiss();
        }
    }

    // 对话框“删除”按钮
    @Override
    public void onConfirm(String tipType) {
        if (mTipDialog != null) {
            mTipDialog.dismiss();
        }
        if (mSelectCount != 0) {
            UserInfoManager.getInstance().deleteUserListInfo(mUserInfoList, mUserListListener,
                    mSelectCount);
        } else {
            updateDeleteUI(false);
        }
        if (mSelectCount == mUserInfoList.size()) {
            // 设置数据库状态
            ShareManager.getInstance(mContext).setDBState(false);
        }
    }

    // 更新删除相关的UI
    private void updateDeleteUI(boolean isShowDeleteUI) {
        if (isShowDeleteUI) {
            mRelativeDelete.setVisibility(View.VISIBLE);
            mImageMenu.setVisibility(View.GONE);
            mImageIconSearch.setVisibility(View.GONE);
            mTextCancel.setVisibility(View.VISIBLE);
            // 列表显示复选框
            mFaceUserAdapter.setShowCheckBox(true);
            mFaceUserAdapter.notifyDataSetChanged();
        } else {
            mRelativeDelete.setVisibility(View.GONE);
            mImageMenu.setVisibility(View.VISIBLE);
            mImageIconSearch.setVisibility(View.VISIBLE);
            mTextCancel.setVisibility(View.GONE);
            // 列表隐藏复选框
            mFaceUserAdapter.setShowCheckBox(false);
            mFaceUserAdapter.notifyDataSetChanged();
            if (mUserInfoList != null) {
                for (int i = 0; i < mUserInfoList.size(); i++) {
                    mUserInfoList.get(i).setChecked(false);
                }
            }
            mCheckAll.setChecked(false);
            mSelectCount = 0;
            mTextDelete.setText("删除");
        }
    }

    private void showPopupWindow(ImageView imageView) {
        if (mPopupMenu != null && imageView != null) {
            if (mUserInfoList == null || mUserInfoList.size() == 0) {
                mPopRelativeImport.setBackgroundColor(Color.parseColor("#777777"));
            } else {
                mPopRelativeImport.setBackgroundColor(Color.parseColor("#666666"));
            }
            int marginRight = DensityUtils.dip2px(mContext, 20);
            int marginTop = DensityUtils.dip2px(mContext, 56);
            mPopupMenu.showAtLocation(imageView, Gravity.END | Gravity.TOP,
                    marginRight, marginTop);
        }
    }

    private void dismissPopupWindow() {
        if (mPopupMenu != null) {
            mPopupMenu.dismiss();
        }
    }

    private void resetDeleteData() {
        mSelectCount = 0;
        for (int i = 0; i < mUserInfoList.size(); i++) {
            mUserInfoList.get(i).setChecked(false);
        }
        mTextDelete.setText("删除");
    }

    // 用于返回读取用户的结果
    private class UserListListener extends UserInfoManager.UserInfoListener {
        // 读取用户列表成功
        @Override
        public void userListQuerySuccess(final String userName, final List<User> listUserInfo) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    mUserInfoList = listUserInfo;
                    if (listUserInfo == null || listUserInfo.size() == 0) {
                        mRelativeEmpty.setVisibility(View.VISIBLE);
                        mRecyclerUserManager.setVisibility(View.GONE);
                        // 显示无内容判断
                        if (userName == null) {
                            mTextEmpty.setText("暂无内容");
                            updateDeleteUI(false);
                        } else {
                            mTextEmpty.setText("暂无搜索结果");
                            mRelativeDelete.setVisibility(View.GONE);
                        }
                        return;
                    }

                    // 恢复默认状态
                    resetDeleteData();
                    mRelativeEmpty.setVisibility(View.GONE);
                    mRecyclerUserManager.setVisibility(View.VISIBLE);
                    if (userName == null || userName.length() == 0) {
                        updateDeleteUI(false);
                    } else {
                        updateDeleteUI(true);
                    }
                    mFaceUserAdapter.setDataList(listUserInfo);
                    mFaceUserAdapter.notifyDataSetChanged();
                }
            });
        }

        // 读取用户列表失败
        @Override
        public void userListQueryFailure(final String message) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mContext == null) {
                        return;
                    }
                    ToastUtils.toast(mContext, message);
                }
            });
        }

        // 删除用户列表成功
        @Override
        public void userListDeleteSuccess() {
            UserInfoManager.getInstance().getUserListInfo(null, mUserListListener);
        }

        // 删除用户列表失败
        @Override
        public void userListDeleteFailure(final String message) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mContext == null) {
                        return;
                    }
                    ToastUtils.toast(mContext, message);
                }
            });
        }
    }

    /**
     * 点击非编辑区域收起键盘
     * 获取点击事件
     */
    @CallSuper
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() ==  MotionEvent.ACTION_DOWN ) {
            View view = getCurrentFocus();
            if (KeyboardsUtils.isShouldHideKeyBord(view, ev)) {
                KeyboardsUtils.hintKeyBoards(view);
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    // ----------------------------------------adapter相关------------------------------------------
    private static class FaceUserViewHolder extends RecyclerView.ViewHolder {
        private View itemView;
        private CircleImageView circleUserHead;
        private TextView textUserName;
        private CheckBox checkView;
        private View viewLine;

        private FaceUserViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            circleUserHead = itemView.findViewById(R.id.circle_user);
            textUserName = itemView.findViewById(R.id.text_user_name);
            checkView = itemView.findViewById(R.id.check_btn);
            viewLine = itemView.findViewById(R.id.view_line);
        }
    }

    public class FaceUserAdapter extends RecyclerView.Adapter<FaceUserViewHolder> implements
            View.OnClickListener, View.OnLongClickListener {
        private List<User> mList;
        private boolean mShowCheckBox;
        private OnItemClickListener mItemClickListener;
        private OnRemoveListener mOnRemoveListener;

        private void setDataList(List<User> list) {
            mList = list;
        }

        private void setShowCheckBox(boolean showCheckBox) {
            mShowCheckBox = showCheckBox;
        }

        private void setItemClickListener(OnItemClickListener itemClickListener) {
            mItemClickListener = itemClickListener;
        }

        public void setOnRemoveListener(OnRemoveListener onRemoveListener) {
            this.mOnRemoveListener = onRemoveListener;
        }

        @Override
        public FaceUserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View view = layoutInflater.inflate(R.layout.item_user_list, parent, false);
            FaceUserViewHolder viewHolder = new FaceUserViewHolder(view);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(FaceUserViewHolder holder, int position) {
            holder.itemView.setTag(position);
            if (position == 0) {
                holder.viewLine.setVisibility(View.GONE);
            } else {
                holder.viewLine.setVisibility(View.VISIBLE);
            }
            // 是否显示多选按钮
            if (mShowCheckBox) {
                holder.checkView.setVisibility(View.VISIBLE);
                if (mList.get(position).isChecked()) {
                    holder.checkView.setChecked(true);
                } else {
                    holder.checkView.setChecked(false);
                }
            } else {
                holder.checkView.setVisibility(View.GONE);
            }
            // 添加数据
            holder.textUserName.setText(mList.get(position).getUserName());
            Bitmap bitmap = BitmapFactory.decodeFile(FileUtils.getBatchImportSuccessDirectory()
                    + "/" + mList.get(position).getImageName());
            Bitmap descBmp = BitmapUtils.calculateInSampleSize(bitmap, 100, 100);
            if (descBmp != null) {
                holder.circleUserHead.setImageBitmap(descBmp);
            }
        }

        @Override
        public int getItemCount() {
            return mList != null ? mList.size() : 0;
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(v, (Integer) v.getTag());
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (mOnRemoveListener != null) {
                mOnRemoveListener.onRemove((Integer) view.getTag());
            }
            return true;
        }
    }

    private void judgeLiveType(int type, Class<?> rgbCls, Class<?> nirCls, Class<?> depthCls) {
        switch (type) {
            case 1: { // 非活体
                startActivity(new Intent(mContext, rgbCls));
                break;
            }

            case 2: { // RGB活体
                startActivity(new Intent(mContext, rgbCls));
                break;
            }

            case 3: { // IR活体
                startActivity(new Intent(mContext, nirCls));
                break;
            }

            case 4: { // 深度活体
                int cameraType = SingleBaseConfig.getBaseConfig().getCameraType();
                judgeCameraType(cameraType, depthCls);
            }
        }
    }

    private void judgeCameraType(int cameraType, Class<?> depthCls) {
        switch (cameraType) {
            case 1: { // pro
                startActivity(new Intent(mContext, depthCls));
                break;
            }

            case 2: { // atlas
                startActivity(new Intent(mContext, depthCls));
                break;
            }

            default:
                startActivity(new Intent(mContext, depthCls));
                break;
        }
    }
}
