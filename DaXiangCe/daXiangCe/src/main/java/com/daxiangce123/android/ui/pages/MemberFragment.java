package com.daxiangce123.android.ui.pages;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.data.AlbumEntity;
import com.daxiangce123.android.data.AlbumMembers;
import com.daxiangce123.android.data.AppData;
import com.daxiangce123.android.data.ConnectInfo;
import com.daxiangce123.android.data.FileEntity;
import com.daxiangce123.android.data.ImageSize;
import com.daxiangce123.android.data.MemberEntity;
import com.daxiangce123.android.data.UserInfo;
import com.daxiangce123.android.helper.DBHelper;
import com.daxiangce123.android.http.ConnectBuilder;
import com.daxiangce123.android.http.Response;
import com.daxiangce123.android.listener.OnTimeLineHeaderActionListener;
import com.daxiangce123.android.listener.OptionListener;
import com.daxiangce123.android.manager.ImageManager;
import com.daxiangce123.android.parser.Parser;
import com.daxiangce123.android.ui.UIManager;
import com.daxiangce123.android.ui.activities.InviteFriendsActivity;
import com.daxiangce123.android.ui.activities.UserDetailActivity;
import com.daxiangce123.android.ui.adapter.MemberAdapter;
import com.daxiangce123.android.ui.pages.base.BaseTabBarFragment;
import com.daxiangce123.android.ui.view.CToast;
import com.daxiangce123.android.ui.view.LoadingDialog;
import com.daxiangce123.android.ui.view.OptionDialog;
import com.daxiangce123.android.util.Broadcaster;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.UMutils;
import com.daxiangce123.android.util.UMutils.ID;
import com.daxiangce123.android.util.Utils;
import com.daxiangce123.android.util.ViewUtil;
import com.daxiangce123.android.util.outh.WXHelper;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class MemberFragment extends BaseTabBarFragment implements OnItemClickListener, OnItemLongClickListener, OptionListener {

    private int requestAlbumCounter;

    private static final String TAG = "MemberFragment";
    private static final int WHAT_ONREFRESHCOMPLETE = 1;
    private Context mContext;
    private View mRootView;
    private ImageView inviteMumber;
    private PullToRefreshListView mPullRefreshListView;
    private ListView mMemberListView;
    private MemberAdapter memberAdapter = null;
    private List<MemberEntity> memberList;

    private boolean deleteMember = false;
    private String creator;
    private DBHelper dbHelper;
    private int startPos;
    private AlbumMembers albumMembers, newAlbumMembers;
    private MemberEntity ownerMember;
    private HashSet<String> userSet;
    private boolean isLoading = false;
    private ArrayList<Integer> mOptionDatas = new ArrayList<Integer>();
    private MemberEntity shouldBeDelete;
    private WXHelper wxHelper;
    private String albumUrl;
    //    private String inviteCode;
    private String albumName;
    private String albumLink;
    private Bitmap bitmap;
    private String albumCoverId;
    private ImageView albumCover;
    private ImageSize imageSize;
    private Bitmap defaultBitmap;
    protected ImageLoadingListener loadingListener = new ImageLoadingListener() {

        @Override
        public void onLoadingStarted(String imageUri, View view) {
        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (imageUri == null) {
                return;
            }

            if (Utils.isEmpty(albumCoverId) || !imageUri.contains(albumCoverId)) {
                return;
            }
            // albumCover.setImageBitmap(loadedImage);
            bitmap = loadedImage;
        }

        @Override
        public void onLoadingCancelled(String imageUri, View view) {
        }
    };


    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                Response response = intent.getParcelableExtra(Consts.RESPONSE);
//                ConnectInfo obj = intent.getParcelableExtra(Consts.REQUEST);
                ConnectInfo info = intent.getParcelableExtra(Consts.REQUEST);
                String content = response.getContent();

                LogUtil.d(TAG, "response = " + response);
                if (Consts.GET_ALBUM_MEMBERS.equals(action)) {
                    // TODO if list is current album???
                    if (!albumEntity.getId().equals(info.getTag())) {
                        return;
                    }
                    showMemberList(content);
                } else if (Consts.LEAVE_ALBUM.equals(action)) {
                    LogUtil.d(TAG, "LEAVE_ALBUM response = " + response);
                    if (response.getStatusCode() == 200 || response.getStatusCode() == 404) {

                        if (!info.getTag().equals("")) {
                            return;
                        }

                        if (info.getTag3() != null) {
                            if (albumEntity.getId().equals(info.getTag3())) {
                                String id = info.getTag2();
                                LogUtil.d(TAG, "LEAVE_ALBUM userId = " + id);
                                if (removeFromList(id)) {
                                    CToast.showToast(R.string.delete_succeed);
                                    // after delete
                                    return;
                                }
                            }
                        }

                    } else {
                        CToast.showToast(R.string.request_failed);
                    }
                } else if (Consts.DELETE_MEMBER.equals(action)) {
                    LogUtil.d(TAG, "DELETE_MEMBER response = " + response);
                    if (response.getStatusCode() == 200 || response.getStatusCode() == 404) {
//                        if (obj instanceof ConnectInfo) {
//                            ConnectInfo info = (ConnectInfo) obj;
                        String id = info.getTag2();
                        LogUtil.d(TAG, "DELETE_MEMBER userId = " + id);
                        if (removeFromList(id)) {
                            CToast.showToast(R.string.delete_succeed);
                            // after delete
                            return;
                        }
//                        }
                    } else {
                        CToast.showToast(R.string.request_failed);
                    }
                } else if (Consts.GET_MEMBER_ROLE.equals(action)) {
                    if (response.getStatusCode() == 200) {
                        MemberEntity member = Parser.parseMember(response.getContent());
                        String albumOwner = null;
                        if (albumEntity != null) {
                            albumOwner = albumEntity.getOwner();
                        }
                        if (member != null && member.getUserId().equals(albumOwner) && member.getRole().equals(Consts.OWNER)) {
                            ownerMember = member;
                            if (userSet.contains(member.getUserId())) {
                                return;
                            }
                            if (!Utils.isEmpty(memberList)) {
                                memberList.add(0, member);
                                memberAdapter.notifyDataSetChanged();
                            }
                        }

                    }
                } else if (Consts.GET_USER_INFO.equals(action)) {
                    if (response.getStatusCode() == 200) {
                        if (ownerInfo != null) {
                            return;
                        }
                        if (App.DEBUG) {
                            LogUtil.d(TAG, "onReceive -> userInfo" + ownerInfo);
                        }

                        // if (albumEntity == null) {
                        // return;
                        // }
                        UserInfo user = Parser.parseUserInfo(content);
                        if (user == null) {
                            return;
                        }
                        if (App.DEBUG) {
                            LogUtil.d(TAG, "onReceive -> user" + user);
                        }

                        if (albumEntity != null) {
                            if (albumEntity.getOwner().equals(user.getId())) {
                                ownerInfo = user;
                                creator = user.getName();
                            }
                        }

                        if (App.DEBUG) {
                            LogUtil.d(TAG, "onReceive -> userInfo=user" + ownerInfo);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void showMemberList(String content) {
        if (App.DEBUG) {
            LogUtil.d(TAG, "showMemberList	" + content);
        }
        LoadingDialog.dismiss();
        isLoading = false;
        sendMessage(WHAT_ONREFRESHCOMPLETE, 50);
        albumMembers = Parser.parseAlbumMembers(content);
        if (albumMembers == null) {
            return;
        }
        LinkedList<MemberEntity> members = albumMembers.getMembers();
        if (Utils.isEmpty(members)) {
            return;
        }
        if (App.DEBUG) {
            StringBuilder sbd = new StringBuilder();
            for (MemberEntity entity : members) {
                if (!Utils.isEmpty(entity.getName())) {
                    continue;
                }
                sbd.append(entity.getUserId());
                sbd.append("	");
                sbd.append(entity.getAlbumId());
                sbd.append("\n");
            }
            LogUtil.d(TAG, "----------------------INVALID MEMBERS	" + sbd);
        }
        if (startPos == 0) {
            memberList.clear();
            userSet.clear();
        }
        for (MemberEntity entity : members) {
            String role = entity.getRole();
            String uid = entity.getUserId();
            if (!userSet.add(uid)) {
                continue;
            }
            if (role.equals(Consts.OWNER)) {
                memberList.add(0, entity);
            } else {
                memberList.add(entity);
            }
        }
        if (ownerMember != null && (!userSet.contains(ownerMember.getUserId()))) {
            userSet.add(ownerMember.getUserId());
            memberList.add(0, ownerMember);
        }
        startPos = startPos + members.size();
        memberAdapter.notifyDataSetChanged();
    }

    private boolean removeFromList(String uid) {
        if (Utils.isEmpty(memberList) || Utils.isEmpty(uid)) {
            LogUtil.d(TAG, "bad paraments " + memberList + " " + uid);
            return false;
        }
        try {
            for (MemberEntity memberEntity : memberList) {
                if (uid.equals(memberEntity.getUserId())) {
                    boolean result = memberList.remove(memberEntity);
                    int members = albumEntity.getMembers() - 1;
                    albumEntity.setMembers(members);
                    memberAdapter.setMemberList(memberList);
                    memberAdapter.notifyDataSetChanged();
                    writeMemberSize();
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        updateTitleViewsContent();
        return false;
    }

    public MemberFragment() {
        memberList = new ArrayList<MemberEntity>();
        userSet = new HashSet<String>();
        if (App.DEBUG) {
            LogUtil.d(TAG, "MemberFragment()	" + this);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = getActivity();
        initBroadcast();
        initOptionData();
    }

    private void initOptionData() {
        mOptionDatas.add(R.string.delete_member);
        mOptionDatas.add(R.string.delete_member_and_block);
    }

    @Override
    public void handleMessage(Message msg) {
        int what = msg.what;
        if (what == WHAT_ONREFRESHCOMPLETE) {
            mPullRefreshListView.onRefreshComplete();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.members_listview, container, false);
            initComponent();
            loadDataFromNet();
        } else {
            ViewUtil.removeFromParent(mRootView);
        }
        initData();
        showOptionBtn();
        return mRootView;
    }

    private void initData() {
        if (albumEntity != null) {
//            inviteCode = albumEntity.getInviteCode();
            albumName = albumEntity.getName();
            albumLink = albumEntity.getLink();
            albumCoverId = albumEntity.getTrueCover();
        }

//        if (inviteCode != null) {
//            mInvitationCode.setText(inviteCode);
//        }
        if (albumCoverId != null) {
            imageSize = new ImageSize(80, 80);
            imageSize.setThumb(true);
            albumCover = new ImageView(getActivity());
            RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(imageSize.getWidth(), imageSize.getHeight());
            albumCover.setLayoutParams(rlp);
            if (bitmap == null && !Utils.isEmpty(albumCoverId)) {
                bitmap = ImageManager.instance().getBitmap(albumCoverId, imageSize, albumCover);
                if (bitmap == null) {
                    loadImage();
                }
            }
        }

        try {
            if (bitmap == null) {
                defaultBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
                bitmap = defaultBitmap;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void loadImage() {
        ImageManager.instance().load(albumCover, albumCoverId, imageSize, null, loadingListener, null);
    }

    public void initComponent() {
        if (App.DEBUG) {
            LogUtil.d(TAG, "--------------initComponent");
        }
        int numColumns = 3;
        int padding = App.SCREEN_WIDTH / 15;

        mPullRefreshListView = (PullToRefreshListView) mRootView.findViewById(R.id.gv_members);
        mPullRefreshListView.setOnRefreshListener(new OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                sendMessage(WHAT_ONREFRESHCOMPLETE, 50);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                handlePullDown(refreshView);
            }
        });
        inviteMumber = (ImageView) mRootView.findViewById(R.id.iv_invite_number);
        inviteMumber.setOnClickListener(this);
        mPullRefreshListView.setMode(Mode.PULL_FROM_END);
        mMemberListView = mPullRefreshListView.getRefreshableView();
        initListHeader(null);
        memberAdapter = new MemberAdapter(getActivity());
        memberAdapter.setMemberList(memberList);
        if (App.DEBUG) {
            LogUtil.d(TAG, " --currentTime-- " + System.currentTimeMillis() + " --initComponent--  " + memberList.size() + " -- memberList.hashCode -- " + memberList.hashCode());
        }
        if (albumEntity != null) {
            memberAdapter.setAlbumEntity(albumEntity);
        }
        mMemberListView.setAdapter(memberAdapter);
        mMemberListView.setOnItemClickListener(this);
        mMemberListView.setOnItemLongClickListener(this);
        mMemberListView.setOnScrollListener(this);
        ViewUtil.ajustMaximumVelocity(mMemberListView, Consts.DEFAUTL_ABS_SCROLL_RATION);

        int paddings = 2 * padding;
        int singleWidth = (App.SCREEN_WIDTH - (numColumns - 1) * padding - paddings) / numColumns;
        ImageSize imageSize = new ImageSize(singleWidth, singleWidth);
        imageSize.setCircle(true);
        memberAdapter.setImageSize(imageSize);

    }

    public void handlePullDown(PullToRefreshBase<ListView> refreshView) {
        if (isLoading) {
            return;
        }
        if (albumMembers == null) {
            return;
        }
        if (albumMembers.hasMore()) {
            isLoading = true;
            if (albumEntity != null) {
                loadMembers(albumEntity.getId(), startPos);
            }
            count("handlePullDown");
        } else {
            startPos = 0;
            CToast.showToast(R.string.no_more);
            sendMessage(WHAT_ONREFRESHCOMPLETE, 50);
        }
    }

    public int getPageSize() {
        return 30;
    }

    private void initBroadcast() {
        IntentFilter ift = new IntentFilter();
        ift.addAction(Consts.GET_ALBUM_MEMBERS);
        ift.addAction(Consts.LEAVE_ALBUM);
        ift.addAction(Consts.GET_MEMBER_ROLE);
        ift.addAction(Consts.DELETE_MEMBER);
        ift.addAction(Consts.GET_USER_INFO);
        ift.addAction(Consts.GET_ALBUM_MEMBERS_DESC);
        Broadcaster.registerReceiver(receiver, ift);
    }

    private void showOptionBtn() {
        if (isJoined) {
            if (albumEntity == null) {
                return;
            }
            if (albumEntity.getOwner() != null && albumEntity.getOwner().equals(App.getUid())) {
                deleteMember = true;
                memberAdapter.setDisplayDeleteMember(deleteMember);
            } else {
                memberAdapter.setDisplayDeleteMember(deleteMember);
            }
        } else {
            memberAdapter.setDisplayDeleteMember(deleteMember);
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.iv_invite_number:
//                showInvite();
                inviteViaWechat();
                break;
        }
    }

    @Override
    public void OnOptionClick(int position, int optionId, Object object) {
        if (shouldBeDelete == null) {
            return;
        }
        if (optionId == R.string.delete_member) {
            showDeleteConfirmDialog(false);
        } else if (optionId == R.string.delete_member_and_block) {
            showDeleteConfirmDialog(true);
        }
    }

    private void showDeleteConfirmDialog(final boolean block) {

        int tip = block ? R.string.confirm_let_x_delete_from_album_and_block : R.string.confirm_let_x_delete_from_album;
        AlertDialog.Builder deleteMember = new AlertDialog.Builder(mContext);
        deleteMember.setMessage(mContext.getString(tip, shouldBeDelete.getName()));
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    if (App.DEBUG) {
                        LogUtil.d(TAG, "deleteMember");
                    }
                    if (block) {
                        ConnectBuilder.leaveAlbum(shouldBeDelete.getAlbumId(), shouldBeDelete.getUserId(), true);
                    } else {
                        ConnectBuilder.deleteMember(shouldBeDelete.getAlbumId(), shouldBeDelete.getUserId());
                    }
                    UMutils.instance().diyEvent(ID.EventRemoveMember);
                }
                dialog.cancel();
            }
        };

        deleteMember.setPositiveButton(R.string.confirm, listener);
        deleteMember.setNegativeButton(R.string.cancel, listener);
        AlertDialog deleteDialog = deleteMember.show();
        TextView textView = (TextView) deleteDialog.findViewById(android.R.id.message);
        if (block) {
            textView.setLines(2);
        }
        textView.setSingleLine(false);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);

    }

    /**
     * execute delete member function and put delete request to server
     *
     * @param memberEntity
     */
    private void deleteMember(final MemberEntity memberEntity) {
        if (App.DEBUG) {
            LogUtil.d(TAG, "deleteMember()	" + memberEntity.getUserId());
        }
        if (memberEntity.getRole().equals(Consts.OWNER)) {
            return;
        }
        shouldBeDelete = memberEntity;
        OptionDialog optionsDialog = new OptionDialog(this.getBaseActivity());
        optionsDialog.setOptionListener(this);
        optionsDialog.setData(mOptionDatas);
        optionsDialog.setLastGrey(false);
        optionsDialog.show();

    }

    private void inviteViaWechat() {
        UMutils.instance().diyEvent(ID.EventInviteViaWechat);
        if (!Utils.isAppInstalled(this.getActivity(), "com.tencent.mm")) {
            CToast.showToast(R.string.not_install_wx);
        }
        if (albumName == null || albumLink == null) {
            return;
        }
        if (wxHelper == null) {
            wxHelper = new WXHelper();
        }
        if (albumUrl == null) {
            albumUrl = Consts.URL_ENTITY_VIEWER + albumLink + "&target=inviteviawechat";
        }
        Log.d(TAG, "albumEntity is albumUrl=" + albumUrl);
        // Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
        // R.drawable.ic_launcher);

        // String description = getString(R.string.intive_via_wechat_x,
        // albumEntity.getName());

        if (!Utils.isEmpty(albumCoverId) && (bitmap == null || bitmap == defaultBitmap)) {
            CToast.showToast(R.string.load_album_cover);

        } else {
            String title = getString(R.string.invite_album_to_weixin_title, albumName);
            String description = getString(R.string.invite_album_to_weixin_summary);
            wxHelper.sendWebPage(albumUrl, bitmap, title, description, false);
            UMutils.instance().diyEvent(ID.EventInviteMemberViaWechatSuccess);
        }
    }

    private void showInvite() {
        if (App.DEBUG) {
            LogUtil.d(TAG, "  token  " + AppData.getToken());
        }
        UMutils.instance().diyEvent(ID.EventInviteMember);
        Bundle bundle = new Bundle();
        if (albumEntity != null) {
            bundle.putParcelable(Consts.ALBUM, albumEntity);
        }
        UIManager.instance().startActivity(InviteFriendsActivity.class, bundle);
    }

    private void onOpenUserDetails(MemberEntity memberEntity) {
        if (memberEntity.getUserId() == null) {
            return;
        }
        if (albumEntity == null) {
            return;
        }
        try {
            Intent intent = new Intent();
            intent.putExtra(Consts.USER_ID, memberEntity.getUserId());
            if (!isJoined) {
                intent.putExtra(Consts.ALBUM, albumEntity);
            }
            intent.putExtra(Consts.ALBUM_ID, memberEntity.getAlbumId());
            intent.putExtra(Consts.IS_JOINED, isJoined);
            intent.setClass(App.getActivity(), UserDetailActivity.class);
            intent.putExtra(Consts.TIME, System.currentTimeMillis());
            mContext.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        try {
            position = position - mMemberListView.getHeaderViewsCount();
            if (position == memberAdapter.getShowNewPosition()) {
                // if (isJoined) {
                showNewMember();
                // }
            } else {
                onOpenUserDetails(memberAdapter.getItem(position));
                if (App.DEBUG) {
                    LogUtil.d(TAG, "memberList -- position: " + position);
                }
                // }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setAlbumEntity(AlbumEntity albumEntity) {
        this.albumEntity = albumEntity;
    }

    public void setIsJoined(boolean isJoined) {
        this.isJoined = isJoined;
    }

    public void setData(UserInfo info) {
        this.ownerInfo = info;
        if (info == null) {
            return;
        }
    }

    private void loadDataFromNet() {
        if (this.albumEntity != null) {
            getAlbumMemberInfo(albumEntity.getId(), albumEntity.getOwner());
        }
        if (ownerInfo == null) {
            if (albumEntity != null) {
                ConnectBuilder.getUserInfo(this.albumEntity.getOwner());
            }
            showLoadingDialog();
        }
        if (ownerInfo != null) {
            creator = ownerInfo.getName();
        }
    }

    private void getAlbumMemberInfo(String albumId, String albumOwner) {
        ConnectBuilder.getMemberRole(albumId, albumOwner);
        showLoadingDialog();
        loadMembers(albumId, 0);
        count("getAlbumMemberInfo");
    }

    private void loadMembers(String albumId, int start) {
        ConnectBuilder.getAlbumMembers(albumId, start, getPageSize());
    }

    @Override
    public String getFragmentName() {
        return "MemberPopupFragment";
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            position = position - mMemberListView.getHeaderViewsCount();
            if (memberAdapter.isDisplayDeleteMember()) {
                deleteMember(memberAdapter.getItem(position));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void onHidden() {
        super.onHidden();
        Log.e(TAG, TAG + "onHidden");
        writeOldMember();
    }

    private void writeMemberSize() {
        if (isJoined) {
            if (dbHelper == null) {
                dbHelper = new DBHelper(App.getUid());
            }
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            HashSet<String> keys = new HashSet<String>();
            keys.add(Consts.MEMBERS);
            albumEntity.setUpdateKey(keys);
            albumEntity.update(db,false);
            db.close();
        }
    }

    private void writeOldMember() {
        if (albumEntity == null) {
            return;
        }
        if (isJoined) {
            if (dbHelper == null) {
                dbHelper = new DBHelper(App.getUid());
            }
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            HashSet<String> keys = new HashSet<String>();
            keys.add(Consts.OLD_MEMBERS);
            albumEntity.setOldMembers(albumEntity.getMembers());
            albumEntity.setUpdateKey(keys);
            albumEntity.update(db,false);
            db.close();
        }
    }

    @Override
    protected void initTabBar() {
        autoLoad = false;
        mRootViews = mRootView;
        refreshableView = mMemberListView;
        addSortDeleteButtonToListHeader = false;
        showBottomBar = false;
        TABBAR_STATE = OnTimeLineHeaderActionListener.STATE_SHOW_MEMBER;
    }

    @Override
    public void onShown() {
        super.onShown();
        if (memberAdapter != null) {
            memberAdapter.notifyDataSetChanged();
        }
    }

    public void refreshData() {
        if (albumEntity == null) {
            return;
        }
        if (this.isVisible()) {
            LoadingDialog.show(R.string.loading);
        }
        loadMembers(albumEntity.getId(), 0);
        count("refreshData");
    }

    private void count(String from) {
        if (!App.DEBUG) {
            return;
        }
        requestAlbumCounter++;
        LogUtil.d(TAG, "----------------------from:" + from + "	count=" + requestAlbumCounter);
    }

    private void showLoadingDialog() {
        LoadingDialog.show(R.string.loading);
    }

    private void showNewMember() {
        if (memberAdapter.getMemberList() == null) {
            return;
        }
        int newMmeberCount = memberAdapter.getNewMemberCount();
        ArrayList<MemberEntity> memberEntities = null;
        albumActivityActionListener.showNewMember(memberEntities, newMmeberCount);
        writeOldMember();
        memberAdapter.notifyDataSetChanged();
        updateRedDotView();
    }

    @Override
    public void onDestroy() {
        Broadcaster.unregisterReceiver(receiver);
        super.onDestroy();
    }

    protected void getFileInPosition(List<FileEntity> currentList, int position) {

    }

}
