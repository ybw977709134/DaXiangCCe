package com.daxiangce123.android.util;

import com.daxiangce123.android.App;
import com.umeng.analytics.MobclickAgent;

import java.util.Map;

/**
 * @author ram
 * @project Yunio-Android
 * @time 2013-7-30
 */
public class UMutils {
    private static boolean DEBUG = true;
    private final static String TAG = "UMutils";

    private static UMutils uMutils;

    private UMutils() {
        if (DEBUG) {
            DEBUG = App.DEBUG;
        }
    }

    public static UMutils instance() {
        if (uMutils == null) {
            uMutils = new UMutils();
        }
        return uMutils;
    }

    public final void diyEvent(ID id) {
        if (id == null) {
            return;
        }
        if (DEBUG) {
            LogUtil.d(TAG, "diyEvent	id=" + id);
        }
        MobclickAgent.onEvent(App.getAppContext(), id.toString());
    }

    public final void diyEvent(ID id, Map<String, String> map) {
        if (id == null || map == null) {
            return;
        }
        if (DEBUG) {
            LogUtil.d(TAG, "diyEvent	id=" + id + " map=" + map);
        }
        MobclickAgent.onEvent(App.getAppContext(), id.toString(), map);
    }

    public enum ID {
        EventSignUpViaMobile,//【注册登录页】 使用手机号注
        EventSignInViaMobile,    //【注册登录页】 使用手机号登录
        EventSignUpViaWechat,       //【注册登录页】 使用微信注册
        EventSignUpViaQQ,      //【注册登录页】 使用QQ注册EventSignUpViaQQ,
        EventSignUpViaWeibo,   //【注册登录页】 使用新浪微博注册
        EventBindingMobile,         //【我页面】 使用第三方帐号注册登录后绑定手机号EventBindingMobile,
        EventModifyMobile,       //【我页面】手机号绑定关系修改
        // EventSocialNetworkAuthSuccess, // 【注册登录】成功获取社交网络授权
        EventSignUpSuccess, // 【注册登录】首次注册登录成功
        EventSignInSuccess, // 【注册登录】老用户再次登录成功
        EventCreateOrJoinAlbum, // 【相册一览页面】点击新建/加入相册的操作
        EventSortAlbumByMod, // 【相册一览页面】相册排序方式切换到“按相册更新时间”
        EventSortAlbumBySize, // 【相册一览页面】相册排序方式切换到“按照片/视频数量”
        EventSortAlbumByOwner, // 【相册一览页面】相册排序方式切换到“按相册拥有者”
        EventRemoveAlbum, // 【相册一览页面】一次删除相册的操作
        EventJoinAlbumByInviteCode, // 【新建/加入相册浮层】点击输入邀请码成功加入相册的操作
        EventJoinAlbumByQrCode, // 【新建/加入相册浮层】点击扫描二维码成功加入相册的操作
        EventCreateAlbum, // 【新建/加入相册浮层】点击新建相册的操作
        EventUploadNow, // 【照片/视频一览页面】新建相册成功后相册内没有照片/视频，一次点击“立即上传”按钮的操作
        EventUpload, // 【照片/视频一览页面】点击上传照片/视频操作
        EventUploadFileSuccess, // 【照片/视频一览页面】成功上传一张照片/视频
        EventUploadFilesSuccess, // 【照片/视频一览页面】成功上传多张照片/视频（另加参数：上传的照片/视频数量）
        // EventViewAlbumInfo, // 【照片/视频一览页面】一次点击相册名称查看相册详情的操作
        EventSortFileByDate, // 【照片/视频一览页面】照片/视频排序方式切换为“时间排序”
        EventSortFileByComment, // 【照片/视频一览页面】照片/视频排序方式切换为“按评论数量”
        EventSortFileByLike, // 【照片/视频一览页面】照片/视频排序方式切换为“按被赞数量”
        EventSharedAlbumToSocialNetworkSuccess, // 【照片/视频一览页面】成功完成一次分享相册到社交网络
        EventRemoveFile, // 【照片/视频一览页面】一次删除照片/视频的操作
        EventTransferAlbum, // 【照片/视频一览页面】一次成功转让相册的操作
        EventClosePush, // 【照片/视频一览页面】一次关闭相册推送的操作
        EventInviteMember, // 【相册成员页面】一次点击邀请成员的操作
        EventRemoveMember, // 【相册成员页面】一次点击删除成员的操作
        EventInviteMemberViaWechatSuccess, // 【邀请成员页面】成功完成一次通过微信邀请好友
        EventInviteMemberViaSMSSuccess, // 【邀请成员页面】成功完成一次通过短信邀请好友
        EventRenameAlbum, // 【相册设置页面】一次更改相册名称的操作
        EventTurnOnAlbumPrivacy, // 【相册设置页面】一次设为私密相册的操作
        EventTurnOnAlbumPassword, // 【相册设置页面】一次开启相册密码的操作
        EventTurnOffAlbumPassword, // 【相册设置页面】一次关闭相册密码的操作
        EventResetAlbumPassword, // 【相册设置页面】一次重置相册密码的操作
        EventTurnOffAlbumComment, // 【相册设置页面】一次关闭照片评论的操作
        EventTurnOffAlbumLike, // 【相册设置页面】一次关闭照片点赞的操作
        EventTurnOffAlbumMemberUpload, // 【相册设置页面】一次关闭成员上传照片的操作
        EventTurnOffAlbumJoinMember, // 【相册设置页面】一次关闭允许加入相册的操作
        EventRemoveFileFormPreview, // 【查看照片页面】一次删除照片的操作
        EventLike, // 【查看照片页面】一次对照片/视频点赞的操作
        EventUnlike, // 【查看照片页面】一次对照片/视频取消点赞的操作
        EventSharedFileToSocialNetworkSuccess, // 【查看照片页面】一次成功分享照片到社交网络的操作
        EventDownloadFile, // 【查看照片页面】一次下载照片/视频的操作
        EventSwipePreview, // 【查看照片页面】一次向左或向右划动来切换照片的操作
        EventSetAlbumCover, // 【查看照片页面】一次设置相册封面的操作
        EventComment, // 【照片详情页面】一次发表评论的操作
        EventRemoveComment, // 【照片详情页面】一次删除评论的操作
        EventReplyComment, // 【照片详情页面】一次回复评论的操作
        // EventFullScreenPreview, // 【照片详情页面】一次点击照片切换到全屏查看单个照片/视频的操作
        EventPreview, // 【个人详细资料页面】一次点击查看单个照片/视频的操作
        EventUserOtherAlbum, // 【个人详细资料页面】一次点击用户其他相册的操作
        EventUserOtherAlbumPreview, // 【个人详细资料页面】一次点击查看用户其他相册的操作
        EventMemberPermissionSet, // 【个人详细资料页面】一次点击设置成员权限的操作
        EventRemoveAllNotifications, // 【通知中心】一次清空所有消息的操作
        EventRemoveNotification, // 【通知中心】一次删除一条消息的操作
        EventPreviewNearbyAlbum, // 【附近相册页面】一次点击查看附近相册的操作
        EventSearchNearbyAlbum, // 【搜索附近相册】一次附近相册搜索的操作（不论是否有搜索结果）
        EventSearchedNearbyAlbumSuccess, // 【搜索附近相册】成功搜索到附近相册
        EventJoinNearbyAlbumSuccess, // 【附近相册详情页】一次成功加入附近相册的操作
        EventJoinHotAlbumSuccess, // 【附近相册详情页】一次成功加入热门相册的操作
        EventJoinPromotedAlbumSuccess, // 【附近相册详情页】一次成功加入活动相册的操作
        EventJoinUserOtherAlbumSuccess, // 【附近相册详情页】一次成功加入用户其他相册的操作
        EventClickHotAlbum, // 【附近相册详情页】一次点击热门相册的操作
        EventClickPromotedAlbum, // 【附近相册详情页】一次点击活动相册的操作
        // EventSwipeNearbyAlbum, // 【附近相册详情页】一次左右划动来切换相册的操作
        EventChangedAvatarSuccess, // 【个人资料页面】一次成功更改头像的操作
        EventRenamedSuccess, // 【个人资料页面】一次成功更改名字的操作
        EventFeedback, // 【个人资料页面】一次点击客服与反馈的操作
        EventAboutUs, // 【个人资料页面】一次点击关于我们的操作
        // EventClearCache, // 【个人资料页面】一次清除缓存的操作
        EventSignOut, // 【个人资料页面】一次登出账户的操作
        EventTurnOnNewAlbumPrivacy, // 【新建相册页面】一次开启私密相册的操作
        EventTurnOnNewAlbumAdvance, // 【新建相册页面】一次点击高级管理的操作
        EventTurnOnNewAlbumPassword, // 【新建相册页面】一次开启相册密码的操作
        EventTurnOffNewAlbumComment, // 【新建相册页面】一次关闭照片评论的操作
        EventTurnOffNewAlbumLike, // 【新建相册页面】一次关闭照片点赞的操作
        EventTurnOffNewAlbumMemberUpload, // 【新建相册页面】一次关闭成员上传照片的操作
        EventTurnOffAlbumMemberJoin, // 【新建相册页面】一次关闭允许加入相册的操作
        EventCreateAlbumSuccess, // 【新建相册页面】一次新建相册成功的操作
        // EventCompletedGuide, // 【新手引导】完成完整的新手引导流程
        // EventSkipGuide, // 【新手引导】点击“跳过”忽略引导；
        // EventHotAlbum, // 【热门相册】一次点击热门相册
        // EventPromotedAlbum, // 【热门相册】一次点击活动相册
        // EventNearbyAlbum, // 【热门相册】一次点击附近相册
        EventSwipeAlbum, // 【查看照片页面】一次向左或向右划动来切换发现相册的操作
        EventUploadOrigin, // 【上传照片/视频页面】一次点击选择原图上传的操作
        EventPWDSETUP, // 【个人资料页面】一次点击锁定密码的操作
        EventSearchLocalAlbum, // 【相册一览页面】一次搜索本地相册的操作（不论是否有搜索结果）
        // EventSearchLocalAlbumSuccess, // 【相册一览页面】一次成功搜索本地相册的操作

        ClickBanner, //
        JoinAlbumViaBanner, //

        EventAutoEnterSourceAlbum, // 自动进入来源相册
        EventJoinAutoEnterSourceAlbumSuccess, // 成功加入自动进入来源相册

        EventTourPressTryTour, // 【引导】点按「碉堡了，试试看」按钮
        EventTourPressSkipTour, // 【引导】点按「我知道了」按钮
        EventTourEnterMyPhotos, // 【引导】进入「我的照片」相册
        EventTourPressUploadPhotos, // 【引导】点按「上传照片」按钮（）
        EventAlbumDetailsPressShareAlbum, // 【相册详情】点按「分享相册」按钮
        EventShareAlbumToWechat, // 【相册详情】点按「分享到微信」按钮
        EventShareAlbumToWechatSuccess, // 分享相册到微信好友成功
        EventShareAlbumToMoments, // 【相册详情】点按「分享到朋友圈」按钮
        EventShareAlbumToMomentsSuccess, // 分享相册到朋友圈成功
        EventShareAlbumToWeibo, // 【相册详情】点按「分享到微博」按钮
        EventShareAlbumToWeiboSuccess, // 分享相册到微博成功
        EventShareAlbumToQzone, // 【相册详情】点按「分享到 QQ 空间」
        EventShareAlbumToQzoneSuccess, // 分享相册到 QQ 空间成功
        //        EventShareAlbumViaSms, // 【相册详情】点按「发送短信」按钮
        EventJoinAlbumFromPreview, // 【照片_视频一览页面】一次点击加入相册按钮的操作
        EventSearchMyAlbum, // 【相册一览页面】 一次点击右上角搜索按钮的操作
        EventSharedFileToSocialNetwork, // 【查看照片页面】一次点击分享按钮的操作

        EventTourPressShareAlbum, // 【引导】点按[分享相册]按钮
        EventTourShareAlbumToWechat, // 【引导】点按[分享到微信]按钮
        EventTourShareAlbumToMoments, // 【引导】点按[分享到朋友圈]按钮
        EventTourShareAlbumToWeibo, // 【引导】点按[分享到微博]按钮
        EventTourShareAlbumToQzone, // 【引导】点按[分享到QQ空间]按钮
        EventTourBindingMobile,//【引导绑定手机页面】 点击绑定手机号的操作


        EventSaveQrcode,//【照片列表页】一次点击二维码保存至本地的操作
        EventInviteViaWechat,//【成员列表页】一次点击邀请微信好友的操作
        EventReadQrcode,//【二维码扫描页】一次点击从相册读取的操作
        EventFindFriends,//【我页面】一次点击查找好友的操作
        EventCheckFriend//   【查找好友页面】一次查看好友的操作
    }

}
