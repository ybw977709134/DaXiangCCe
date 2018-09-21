package com.daxiangce123.android;

/**
 * @author ram
 * @project Groubum
 * @time Feb 25, 2014
 */
public class Consts {
    public static final int CONFIG_ALBUM_CACHE_LIMIT = 10;

    public static final float FILE_UPLOAD_LIMIT = 1024 * 1203 * 99f;
    public static final double EARTH_RADIUS = 6378137.0;
    public static final int SEC_IN_MILLS = 1000;
    public static final int MIN_IN_MILLS = 60 * SEC_IN_MILLS;
    public static final int HOU_IN_MILLS = 60 * MIN_IN_MILLS;
    public static final int DAY_IN_MILLS = 24 * HOU_IN_MILLS;
    public static final int DAY_IN_MILLS_3 = 3 * DAY_IN_MILLS;
    public static final int REQUEST_CODE = 10086;
    public static final int REQUEST_CODE_CAMERA_IMAGE = 10087;
    public static final int REQUEST_CODE_CAMERA_VIDEO = 10088;
    public static final int REQUEST_CODE_ZXING = 10089;
    public static final int REQUEST_CODE_CHOOSE_IMAGE = 10090;
    public static final int REQUEST_CODE_CREATE_ALBUM = 10091;
    public static final int REQUEST_CODE_FIND_FRIEND = 10092;
    public static final int IM_PORT = 5222;
    public static final int IO_BUFFER_SIZE = 16 * 1024;
    public static final int PROGRESS_DELTA = 100; // 250ms
    public static final int HIDE_SHOW_ANIMATION_DURATION = 200;
    public static final int UIL_THREADPOOL_SIZE = 5;

    public static final int KB_PRE_MB = 1024;
    public static final int BYTE_PRE_KB = 1024;
    public static final int BYTE_PRE_MB = 1024 * 1024;

    public static final int TIMEOUT_LONG = 60000; // 1 minute
    public static final int TIMEOUT_SHORT = 30000;// 30 seconds
    public static final int BOOLEAN_TRUE = 1;
    public static final int BOOLEAN_FALSE = 0;
    public static final int IO_PERMISSION_R = 1;
    public static final int IO_PERMISSION_W = 2;

    public static final int DEFAUTL_ABS_SCROLL_RATION = 2;
    public static final float SLOW_ABS_SCROLL_RATION = 2.5f;

    public static final int RESPONSE_STATUS_DEFAULT = -1;

    public static final int VIDEO_DURATION_MAX = 61 * 1000;
    public static final int VIDEO_DURATION_MIN = 5 * 1000;

    public static final String INTERNAL = "internal";
    public static final String EXTERNAL = "external";
    public static final String PRODUCT = "product";

    // public static String BAIDU_AK = "CKTfsUZ4UhsOFAu9Y8RDnXDf";//S
    public static final String BAIDU_AK = "VSSQpu2hearvI70DFwGNN2wm";// A
    // public static final String BAIDU_AK = "CtcL7wYu6W0pBvvblVGBR2sF";// T
    public static final String CAPTURE_IMAGE_TIME_FORMAT = "yyyy-MM-dd-hh-mm-ss";
    public static final String SERVER_UTC_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";// 2014-03-12T03:12:59.000Z
    public static final String FILE_OPEN_MODE_R = "r";
    public static final String FILE_OPEN_MODE_RW = "rw";
    public static final String FILE_OPEN_MODE_RWS = "rws";
    public static final String FILE_OPEN_MODE_RWD = "rwd";

    public static final String HAS_LOGINED = "has_logined";
    public static final String SSO = "sso";
    public static final String SSO_BIND = "SSO_BIND";
    public static final String SSO_PROVIDER = "provider";
    public static final String TYPE = "type";
    public static final String TOKEN = "token";
    public static final String TWITTER_TOKEN = "twitter_token";
    public static final String TWITTER_SECRET = "twitter_secret";
    public static final String SCHEME_FILE = "file";
    public static final String SCHEME_HTTP = "http";
    public static final String REQUEST = "request";
    public static final String RESPONSE = "response";

    public static final String ON_HANLDE_ALBUM_LIST = "on_hanlde_album_list";
    public static final String ON_HANLDE_TEMP_TOKEN = "on_hanlde_temp_token";
    public static final String LIST_ALL_ALBUMS = "list_all_albums";
    public static final String TWITTER_CONSUMER_KEY = "YRkfAGoE4hwhAz9orSrw";
    public static final String TWITTER_CONSUMER_SECRET = "xrtIA4K9IGsDgtIYwYKZrclg8PW7loEIbw4BrdBcWOo";
    public static final String TWITTER_CALLBACK_URL = "oauth://dev.cliq123.com";
    public static final String LOCATION = "location";
    public static final String BAIDU_API_KEY = "CKTfsUZ4UhsOFAu9Y8RDnXDf";
    public static final String FROM_STARTER = "from_starter";
    public static final String HANDLE_EVENTS = "handle_events";

    public static String HOST_HTTPS;
    public static String HOST_HTTP;
    public static String URL_ENTITY_VIEWER;
    public static String URL_ENTITY_RAW;
    public static String URL_AGREE;
    public static String URL_PRIVACY;
    public static String URL_RECOMMEND_APPS;
    public static String URL_RECOMMEND_APPS_STAUTS;
    public static String URL_GET_ALBUM_QR;
    public static String URL_GET_ACTIVITY_PAGE;
    public static String URL_ACTIVITY_PAGE;
    public static final String CONTENT_RANGE = "Content-Range";
    public static final String USER_AGENT = "User-Agent";
    public static final String AUTHORIZATION = "Authorization";
    public static final String USER_INFO = "user_info";
    public static final String UPDATE_USER_GEO = "update_user_geo";
    public static final String LIST_NEARBY_ALBUM = "list_nearby_album";
    public static final String LIST_HOT_ALBUM = "list_hot_album";
    public static final String LIST_HOT_ALBUM_HOT = "list_hot_album_hot";
    public static final String LIST_HOT_ALBUM_PRO = "list_hot_album_pro";

    public static final String SOURCE = "Source";
    public static final String SOURCE_ANDROID = "android";

    public static final String URL = "url";
    public static final String BASIC_CONFIG = "basic_config";
    public static final String AUTH = "Authorization";
    public static final String OAUTH2 = "OAuth2 ";
    public static final String ACCEPT = "Accept";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String CONTENT_LENGTH = "Content-Length";
    public static final String RANGE = "Range";
    public static final String EXPECT = "Expect";
    public static final String COOKIE = "Cookie";
    public static final String COOKIE_PREFIX = "cliq_sess_id=";
    public static final String CHARSET_UTF_8 = "UTF-8";
    public static final String PROGRESS_INFO = "progress_info";
    public static final String TRANSFER_PROGRESS = "transfer_progress";
    public static final String TRANSFER_INFO = "transfer_info";
    public static final String TRANSFER_RESULT = "transfer_result";
    public static final String ID = "id";
    public static final String ALBUM_ID = "album_id";
    public static final String ALBUM_NAME = "album_name";
    public static final String NAME = "name";
    public static final String NOTE = "note";
    public static final String ACCESS_PASSWORD = "access_password";
    public static final String IS_PRIVATE = "is_private";
    public static final String IS_LOCKED = "is_locked";
    public static final String COMMENT_OFF = "comment_off";
    public static final String LIKE_OFF = "like_off";
    public static final String PERMISSIONS = "permissions";
    public static final String VIEWS = "views";
    public static final String PERMISSIONS_CHECKED = "permission_checked";
    public static final String CREATE_DATE = "create_date";
    public static final String EXPIRE_DATE = "expire_date";
    public static final String MOD_DATE = "mod_date";
    public static final String START_DATE = "start_date";
    public static final String END_DATE = "end_date";
    public static final String CREATOR = "creator";
    public static final String OWNER = "owner";
    public static final String MEMBER = "member";
    public static final String LINK = "link";
    public static final String SIZE = "size";
    public static final String MEMBERS = "members";
    public static final String OLD_MEMBERS = "old_members";
    public static final String ALBUM = "album";
    public static final String ALBUM_SORT = "album_sort";
    public static final String FILE_SORT = "file_sort";
    public static final String INVITE_CODE = "invite_code";
    public static final String UPDATE_COUNT = "update_count";
    public static final String ALBUMS = "albums";
    public static final String BANNERS = "banners";
    public static final String HAS_MORE = "has_more";
    public static final String LIMIT = "limit";
    public static final String FILE = "file";
    public static final String FILE_ID = "file_id";
    public static final String FILES = "files";
    public static final String FOLDER = "folder";
    public static final String POSITION = "position";
    public static final String COVER = "cover";
    public static final String CURRENT_TITLE = "current_title";
    public static final String CURRENT_LINK = "current_link";
    public static final String ROLE = "role";
    public static final String TIME = "time";
    public static final String NUMBER = "number";
    public static final String EVENTS = "events";
    public static final String EVENT_LIST = "event_list";
    public static final String OBJ_ID = "obj_id";
    public static final String OBJ_TYPE = "obj_type";
    public static final String MSG = "msg";
    public static final String REPLY_TO_USER_NAME = "reply_to_user_name";
    public static final String REPLY_TO_USER = "reply_to_user";
    public static final String USER_NAME = "user_name";
    public static final String MAX_CHOOSEN = "max_choosen";
    public static final String CURRENT_SELECT = "current_select";
    public static final String DISTANCE = "distance";
    public static final String TEMP_TOKEN = "temp_token";
    public static final String PUSH = "push";
    public static final String NO_PUSH = "no_push";
    public static final String THUMB_FILE_ID = "thumb_file_id";
    public static final String THUMB_ID = "thumb_id";
    // public static final String SHARES = "shares";
    // public static final String DOWNLOADS = "downloads";
    public static final String WX_ACCESS_TOEKN = "wx_access_toekn";
    public static final String GET_WX_UNION_ID = "get_wx_union_id";
    public static final String UNIQUE_ID = "unique_id";
    public static final String UNION_ID = "union_id";
    /**
     * broadcast action that location has located
     */
    public static final String ACTION_LOCATED = "action_located";

    public static final String STOP_EVENT_SERVICE = "stop_event_service";
    public static final String START_EVENT_SERVICE = "start_event_service";
    public static final String START_UPLOADING_CHECKING = "start_uploading_check";

    public static final String STOP_FETCH_EVENT_SERVICE = "stop_fetch_event_service";

    public static final String VIDEO_PATH = "video_path";
    public static final String DELETED_FILE_ID = "deleted_file_id";
    public static final String ACCESS_TOKEN_TAG = "#access_token_tag#";

    public static final String EMAIL = "email";
    public static final String EMAIL_VERIFIED = "email_verified";
    public static final String EVENT_ID = "event_id";
    public static final String CREATED_AT = "created_at";
    public static final String CREATED_DATE = "created_date";
    public static final String SRC_DEVICE = "src_device";
    public static final String OP_TYPE = "op_type";
    public static final String OBJECT = "object";
    public static final String SHOW_NONE = "show_none";
    public static final String SHOW_EMOJI = "show_emoji";
    public static final String SHOW_EDIT = "show_edit";
    public static final String HINT = "hint";
    public static final String MOBILE = "mobile";
    public static final String MOBILE_VERIFIED = "mobile_verified";
    public static final String LEVEL = "level";
    public static final String STATUS = "status";
    public static final String GENDER = "gender";
    public static final String LANG = "lang";
    public static final String QUATA = "quata";
    public static final String NEED_VIDEO = "need_video";
    public static final String DISABLE_PHOTO_PREVIEW = "disable_photo_preview";
    public static final String ORGINIAL_IMAGE = "orginial_bitmap";
    public static final String NOTIFICATION = "notification";
    public static final String ALBUM_NOTIFY = "album_notify";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String ACCURACY = "accuracy";
    public static final String HAS_THUMB = "has_thumb";
    public static final String OFFSET = "offset";

    public static final String MESSAGE = "message";
    public static final String REQ_ID = "req_id";
    public static final String CODE = "code";

    public static final String ACCESS_TOKEN = "access_token";
    public static final String TOKEN_TYPE = "token_type";
    public static final String EXPIRES_IN = "expires_in";
    public static final String SCOPE = "scope";
    public static final String STATE = "state";
    public static final String UPLOADING_FILE = "uplaoding_file";
    public static final String UPLOAD_FILE = "upload_file";
    public static final String CREATE_FILE = "create_file";
    public static final String DELETE_FILE = "delete_file";
    public static final String DOWNLOAD_FILE = "download_file";
    public static final String TITLE = "title";
    public static final String DIGEST = "digest";
    public static final String MIME_TYPE = "mime_type";
    public static final String BATCH_ID = "batch_id";
    public static final String BATCHES = "batches";
    public static final String SEQ_NUM = "seq_num";
    public static final String CURRENT_USER = "current_user";
    // public static final String BITMAP_READY = "bitmap_ready";
    public static final String TAG = "tag";
    public static final String ZXING_RESULT = "zxing_result";
    public static final String PATH_LIST = "path_list";
    public static final String FILE_LIST = "FILE_LIST";
    public static final String DEVICE = "device";
    public static final String DEVICE_ID = "device_id";
    public static final String COMMENT = "comment";
    public static final String COMMENTS = "comments";
    public static final String LIKES = "likes";
    public static final String FILE_PATH = "file_path";
    public static final String ALBUMITEMS = "albumitems";
    public static final String IMAGE_SIZE = "image_size";
    public static final String IMAGE_KEY = "image_key";
    public static final String IMAGE_LIST = "image_list";
    public static final String USER_ID = "user_id";
    public static final String PASSWORD = "password";
    public static final String LIST_ALBUM = "list_album";
    public static final String LIST_OTHER_USER_ALBUM = "list_other_user_album";
    public static final String LIST_BANNER = "list_banner";
    public static final String GET_BANNER = "get_bannner";
    public static final String JOIN_ALBUM = "join_album";
    public static final String JOIN_ALBUM_SAMPLE = "join_album_sample";
    public static final String GET_DWURL = "get_dwurl";
    public static final String GET_AVATAR = "get_avatar";
    public static final String GET_BATCHES = "get_batches";
    public static final String GET_FILE_INFO = "get_file_info";
    public static final String GET_USER_INFO = "get_user_info";
    public static final String GET_MIME_INFO = "get_mime_info";
    public static final String GET_ALBUM_ITEMS = "get_album_items";
    public static final String GET_ALBUM_UPDATE_ITEMS = "get_album_update_items";
    public static final String GET_NON_MEMBER_ALBUM_ITEMS = "get_non_member_album_items";
    public static final String GET_USER_OF_ALBUM_ITEMS = "get_user_of_album_items";
    public static final String GET_ALBUM_COVER = "get_album_cover";
    public static final String GET_NONE_ALBUM_COVER = "get_none_album_cover";
    public static final String GET_ALBUM_SAMPLE_COVER = "get_album_sample_cover";
    public static final String GET_ALBUM_MEMBERS = "get_album_members";
    public static final String GET_ALBUM_MEMBERS_DESC = "get_album_members_asc";
    public static final String GET_MEMBER_ROLE = "get_member_role";
    public static final String CREATE_ALBUM = "create_album";
    public static final String GET_EVENTS = "get_events";
    public static final String SEARCH_ALBUM = "search_album";
    public static final String GET_ALBUM_SAMPLES = "get_album_samples";
    public static final String GET_TEMP_TOKEN_BY_LINK = "get_temp_token_by_link";
    public static final String REGISTRATION_ID = "registration_id";
    public static final String CHANNEL_ID = "channel_id";
    public static final String PROVIDER = "provider";
    public static final String OS_TYPE = "os_type";
    public static final String OS = "os";

    public static final String BANNER_ID = "banner_id";
    public static final String CLOSE_DATE = "close_date";

    public static final String APP_LOCK_TIME = "app_lock_time";

    public static final String ALBUM_CREATED = "album_created";
    public static final String ALBUM_DELETED = "album_deleted";
    public static final String ALBUM_UPDATED = "album_updated";
    public static final String ALBUM_SHARED = "album_shared";
    public static final String FILE_CREATED = "file_created";
    public static final String FILE_DELETED = "file_deleted";
    public static final String FILE_UPDATED = "file_updated";
    public static final String FILE_SHARED = "file_shared";
    public static final String FILE_DOWNLOADED = "file_downloaded";
    public static final String COMMENT_CREATED = "comment_created";
    public static final String COMMENT_DELETED = "comment_deleted";
    public static final String REPORT_COMMENT = "report_comment";
    public static final String LIKE_CREATED = "like_created";
    public static final String LIKE_DELETED = "like_deleted";
    public static final String MEMBER_JOINED = "member_joined";
    public static final String MEMBER_LEFT = "member_left";
    public static final String MEMBER_UPDATED = "member_updated";
    public static final String USER_UPDATED = "user_updated";
    public static final String AVATAR_UPDATED = "avatar_updated";
    public static final String FILE_NOTIFY = "file_notify";
    public static final String TEMP_ACCESS_TOKEN = "temp_access_token";

    public static final String METHOD_GET_BATCHES = "/album/batches";
    public static final String METHOD_GET_METHOD_TEMP_TOKEN = "/temp/token";
    public static final String METHOD_BIND = "/user/oauth2/bind";
    public static final String METHOD_CREATE_ALBUM = "/album";
    public static final String METHOD_LIST_ALBUM = "/album/list";
    public static final String METHOD_LIST_BANNER = "/banner/list";
    public static final String METHOD_GET_BANNER = "/banner";
    public static final String METHOD_DELETE_ALBUM = "/album";
    public static final String DELETE_ALBUM = "delete_album";
    public static final String METHOD_GET_MINE_INFO = "/user/me";
    public static final String METHOD_GET_USER_INFO = "/user";
    public static final String METHOD_GET_FILE_INFO = "/file";
    public static final String METHOD_JOIN_ALBUM = "/join";
    public static final String METHOD_GET_ALBUM_ITEMS = "/album/items";
    public static final String METHOD_GET_ALBUM_THUM_ID = "/album/thumbid";
    public static final String METHOD_GET_ALBUM_MEMBERS = "/member";
    public static final String METHOD_DOWNLOAD_AVATAR = "/user/avatar";
    public static final String METHOD_GET_MEMBER_ROLE = "/member";
    public static final String METHOD_CREATE_NEW_FILE = "/file";
    public static final String METHOD_UPLOAD_CONTENT = "/file/content";
    public static final String METHOD_DOWNLOAD_THUMB = "/thumb";
    // public static final String METHOD_DOWNLOAD_SAMPLE_THUMB =
    // "/thumb/sample";
    public static final String METHOD_DOWNLOAD_FILE = "/file/content";
    public static final String METHOD_GET_FIEL_DWRUL = "/file/dwurl";
    public static final String METHOD_DELETE_FILE = "/file";
    public static final String METHOD_GET_EVENTS = "/event";
    public static final String METHOD_POST_COMMENTS = "/file/comment";
    public static final String METHOD_UNBIND_DEVICE = "/user/dev/unbind";
    public static final String METHOD_UPDATE_MINE_INFO = "/user/me";
    public static final String METHOD_SET_AVATAR = "/user/avatar";
    public static final String METHOD_GET_COMMENTS = "/file/comment";
    public static final String METHOD_DELETE_COMMENTS = "/file/comment";
    public static final String METHOD_SET_MEMBER_ROLE = "/member";
    public static final String METHOD_LEAVE_ALBUM = "/member";
    public static final String METHOD_DISLIKE_FILE = "/file/like";
    public static final String METHOD_LIKE_FILE = "/file/like";
    public static final String METHOD_HAS_LIKED = "/file/like";
    public static final String METHOD_CHECK_ALBUN_ACCESS_CONTROL = "/album/checkaccess";
    public static final String METHOD_UPDATE_USER_GEO = "/user/geo";
    public static final String METHOD_LIST_NEARBY = "/album/nearby";
    public static final String METHOD_LIST_HOT_ALBUM = "/hot/list";
    public static final String CHECK_ALBUM_ACCESS_CONTROL = "check_album_access_control";
    public static final String CHECK_ALBUM_TEMP_ACCESS_CONTROL = "check_album_temp_access_control";
    public static final String UNBIND_DEVICE = "unbind_device";
    public static final String APP_LOCK_UNBIND_DEVICE = "app_lock_unbind_device";
    public static final String FORCE_UNBIND_DEVICE = "force_unbind_device";
    public static final String UPDATE_MIME_INFO = "update_mime_info";
    public static final String METHOD_RESET_MEMBER_PERMISSION = "/member/permissions";
    public static final String METHOD_SEARCH_ALBUM = "/search/album";
    public static final String METHOD_ALBUM_THUMB = "/album/thumb";
    public static final String METHOD_GET_LIKE = "/file/like";
    public static final String METHOD_GET_ALBUM_SAMPLES = "/album/samples";
    public static final String METHOD_DOWN_BANNER = "/banner/content";
    public static final String METHOD_ACCESS_BY_LINK = "/link";
    public static final String METHOD_FINISH_BATCH = "/file/batch";
    public static final String METHOD_REGI_PUSH = "/user/push";
    public static final String METHOD_NO_PUSH = "/album/nopush";
    public static final String UNREGI_PUSH = "unregi_push";
    public static final String REGI_PUSH = "regi_push";

    public static final String SET_AVATAR = "set_avatar";
    public static final String GET_COMMENTS = "get_comments";
    public static final String GET_LIKE = "get_like";
    public static final String LIKE = "like";
    public static final String LIKE_LIST = "like_list";
    public static final String POST_COMMENT = "post_comment";
    public static final String DELETE_COMMENT = "delete_comment";
    public static final String LIKE_FILE = "like_file";
    public static final String DISLIKE_FILE = "dislike_file";
    public static final String HAS_LIKED = "has_liked";

    public static final String METHOD_UPDATE_ALBUM = "/album";
    public static final String UPDATE_ALBUM = "update_album";
    public static final String SET_MEMBER_ROLE = "set_role";
    public static final String LEAVE_ALBUM = "leave_album";
    public static final String DELETE_MEMBER = "delete_member";

    public static final String GET_ALBUM_ACT = "get_album_activity";
    public static final String METHOD_GET_ALBUM_ACTIVITY = "/album/activity";
    public static final String PICTURES = "pictures";
    public static final String PHOTO_VIEWER_FRAGMENT = "photo_viewer_fragment";
    public static final String VIDEO = "video";
    public static final String ON_LIST_ALBUMS = "on_list_albums";
    public static final String ON_LIST_ALBUMS_ITEMS = "on_list_albums_items";
    public static final String NEED_PASSWORD = "need_password";
    public static final String RESET_MEMBER_PERMISSION = "reset_member_permissions";

    public static final String IS_SAMPLE_MODE = "is_sample_mode";

    public static final String HOT_ALBUM = "hot_album";
    public static final String PROMOTED_ALBUM = "promoted_album";
    public static final String NEARBY_ALBUM = "nearby_album";

    public static final String SET_ALBUM_THUMB = "set_album_thumb";
    public static final String METHOD_SET_ALBUM_THUM = "/album/thumb";

    public static final String METHOD_SHARED_ALBUM = "/album/share";
    public static final String SHARED_ALBUM = "shared_album";
    public static final String SHARED_TO = "share_to";
    public static final String SHARES = "shares";

    public static final String METHOD_SHARED_FILE = "/file/share";
    public static final String METHOD_DOWNLOAD_FILE_COUNT = "/file/download";
    public static final String SHARED_FILE = "shared_file";
    public static final String DOWNLOADED_FILE = "downloaded_file";
    public static final String DOWNLOADS = "downloads";
    public static final String GET_SENSITIVE_WORD = "get_method_list_sentive_word";
    public static final String METHOD_LIST_SENTIVE_WORD = "/sensitive_word";
    public static final String WORDS = "words";
    public static final String WORD = "word";
    public static final String METHOD_CONTACT = "/contact";
    public static final String METHOD_LIST_CONTACT = "/contact/list";
    public static final String GET_CONTACTS = "get_contact";
    public static final String DELETE_CONTACT = "delete_contact";
    public static final String CREATE_CONTACT = "create_contact";
    public static final String FRIEND_NAME = "friend_name";
    public static final String CONTACTS = "contacts";
    public static final String CONTACT = "contact";
    public static final String CONTACT_ID = "contact_id";
    public static final String REG_USER_ID = "reg_user_id";
    public static final String PHOTO_URI = "photoUri";


    public static final String MASK_HAS_SHOW = "mask_has_show";

    public static final String CAPTURE_PICTURE_LISTENER = "capture_picture_listener";

    public static final String STATUS_CREATE = "created";
    public static final String STATUS_EXISTED = "existed";
    public static final String LOG_OUT = "log_out";
    public static final String FIRST_LAUNCH = "first_launch";
    public static final String FILEENTITY_STATUS_UNUPLOAD = "unupload";
    public static final String FILEENTITY_STATUS_UPLOADING = "uploading";
    public static final String FILEENTITY_STATUS_ACTIVE = "active";

    public static final String IS_JOINED = "is_joined";
    public static final String LOGIN_WX_SUCCEED = "login_wx_succeed";
    public static final String LOGIN_WX_TOKEN = "login_wx_TOKEN";
    public static final String SHARE = "share";
    public static final String INVITE = "invite";

    public static final String METHOD_GET_ALBUM_INFO = "/album";
    public static final String GET_ALBUM_INFO = "get_album_info";
    public static final String GET_PIN_TO_WEB = "get_pin_to_web";
    public static final String GET_RECOMMEND_APP_STATUS = "get_recommend_app_status";
    //    public static final String GET_ALBUM_QR = "get_album_qr";
    public static final String GET_ACTIVITY_PAGE = "get_activity_page";
    public static final String NOT_OPEN_ALBUM = "not_open_album";
    public static final String COMMENT_FILE = "comment_file";

    public static final String NETWORK_STATE_CHANGED = "network_state_changed";

    public static final String METHOD_POST_REPORT = "/report";
    public static final String POST_REPORT = "post_report";
    public static final String USER = "user";
    public static final String REPORT_REASON = "report_reason";

    public static String NOT_REGISTER = "not_register";
    public static String ONE_DAY_NOT_LAUNCH = "one_day_not_launch";
    public static String TWO_DAYS_NOT_LAUNCH = "two_days_not_launch";
    public static String THREE_DAYS_NOT_LAUNCH = "three_days_not_launch";
    public static final String QUIT_APP_TIME = "quit_time";

    public static final String DISABLED_TEMPORARILY = "disabled_temporarily";
    public static final String DISABLED_PERMANENTLY = "disabled_permanently";

    public static final String SYSTEM_USER_DISABLED = "system_user_disabled";
    public static final String MOBILE_LOGIN_INTERRUPT = "mobile_login_interrupt";
    public static final String OAUTH_INTERRUPT = "Oauth_interrupt";

    public static final String SYSTEM_FILE_DELETED = "system_file_deleted";
    public static final String SYSTEM_ALBUM_DELETED = "system_album_deleted";
    public static final String LOGIN_TIMEOUT = "login_timeout";

    public static final String WEI_XIN = "wei_xin";

    public static final String FIRST_UPLOAD_TIME = "first_upload_time";
    public static final String FIRST_UPLOAD = "first_upload";
    public static final String HAS_SHOW_UPLOAD_SHARE_GUIDE = "has_show_upload_share_guide";
    public static final String HAS_SHOW_SHARE_GUIDE = "has_show_share_guide";
    public static final String FIRST_OPEN_ALBUM = "first_open_album";
    public static final String IS_FIRST_BIND = "is_first_bind";
    public static final String SHOW_NEW_USER_GUIDE = "show_new_user_guide";

    // public static final String LOCK_CODE = "lock_code";

    public static final String SHOW_BULLET = "show_bullet";
    public static final String VISITOR_UID = "0000-00000000-0000-0000-0000-000000000000";
    public static final String READ_CONTACT_SHOW_TIME = "restime";
    public static final String READ_CONTACT = "read_contact";
    public static final String SHOW_CONTACT_GRUID = "SHOW_CONTACT_GRUID";

    public static final String ALBUM_QR_SWITCH_NUM = "album_qr_switch_num";

    public static final String DOWNLOAD_FILE_TIME = "download_file_time";

    public static final String BIND_PHONE_GUIDE_TIME = "bind_phone_guide_time";

    public static final String METHOD_LIST_BINDINGS = "/user/oauth2/bindings";
    public static final String LIST_BINDINGS = "list_bindings";
    public static final String BINDINGS = "bindings";

    public static final String METHOD_LIST_REGION = "/region/list";
    public static final String LIST_REGION = "list_region";
    public static final String REGION = "region";
    public static final String REGIONS = "regions";

    public static final String METHOD_RESET_PASSWORD = "/user/password";
    public static final String RESET_PASSWORD = "reset_password";
    public static final String CONFIRMATION = "confirmation";

    public static final String CONFIRMATION_CODE_EXPIRED = "confirmation code expired";

    public static final String METHDO_REQUEST_CONFIRMATION = "/user/confirmation";
    public static final String REQUEST_CONFIRMATION = "request_confirmation";
    public static final String PURPOSE = "purpose";

    public static final String METHOD_CREATE_NEW_USER = "/user";
    public static final String CREATE_NEW_USER = "create_new_user";

    public static final String METHOD_UNBIND_SSO = "/user/oauth2/bind";
    public static final String UNBIND_SSO = "unbind_sso";

    public static final String METHOD_MOBILE_LOGIN = "/user/oauth2/access_token";
    public static final String MOBILE_LOGIN = "mobile_login";
    public static final String ERROR_MESSAGE_INVALID_USERNAM = "invalid email, mobile";

    public static final String METHDO_UPDATE_USER_INFO = "/user/me";
    public static final String CHANGE_PASSWORD = "change_password";
    public static final String OLD_PASSWORD = "old_password";
    public static final String NEW_PASSWORD = "new_password";

    public static final String CHECK_CONFIRMATION = "check_confirmation";
    public static final String METHOD_CHECK_CONFIRMATION = "/user/confirmation";

    public static final String METHOD_GET_USER_ID_BY_AUTHENTIC = "/user/oauth2/access_token/user_id";
    public static final String GET_USER_ID_BY_AUTHENTIC = "get_user_id_by_authentic";
    public static final String METHOD_GET_USER_ID_BY_OAUTH = "/user/oauth2/bind/user_id";
    public static final String GET_USER_ID_BY_OAUTH = "get_user_id_by_oauth";
    public static final String METHOD_GET_USER_SUSPENDED_INFO = "/suspend/user";
    public static final String GET_USER_SUSPENDED_INFO = "get_user_suspended_info";


    public static final String regEx = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？\\ ]";
    public static final String CLICK_NUM = "click_num";
    public static final String BACKGROUND_COLOR = "background_color";
    public static final String USE_BACKGROUND_COLOR = "use_background_color";
    public static final String FILECONTENT = "/file/content";
    public static final String Cartoons = "cartoons";
    public static final String SPLASH_LIST = "/splash/list";
    public static final String LIST_SPLASH_AD = "list_splash_ad";
    public static final String DOWNLOAD_SPLASH = "download_splash";
    public static final String SPLASH_CLICK = "splash_click";
    public static final String APK_DOWNLOAD = "apk_download";
    public static final String SPLASH = "/splash/";
    public static final String SPLASH_BG_COLOR = "splash_bg_color";
    public static final String CLICK = "/click";
    public static final String APLICATION_ARCHIVE = "application/vnd.android.package-archive";




    public static enum PROVIDERS {
        google, facebook, weibo, qq, wechat
    }

    public static enum PURPOZE {
        password, registration
    }

    public static enum HttpMethod {
        NONE, GET, PUT, POST, DELETE,
    }

    ;

    public static enum Priority {
        HIGH, NORMAL, LOW,
    }

    ;

    public static enum Sort {
        BY_NAME, // name
        BY_CREATE_DATE, // create date
        BY_MOD_DATE, // modify date
    }

    public static enum Order {
        ASC, // asc
        DESC, // desc
    }

    // public static enum Role {
    // OWNER, // owner
    // ADMIN, // admin
    // MEMBER, // member
    // }

    public static enum AlbumSort {
        // UPDATED_COUNT, CREATED_DATE, ITEM_COUNT, OWNER
        UPDATED_DATE, ITEM_COUNT, OWNER
    }

    public static enum FileSort {

        TIMELINE_SORT("by_mod_date", "CREATE_DATE"), COMMENTS_SORT("by_comment_num", "COMMENTS"), LIKE_SORT("by_like_num", "LIKES");
        private String server_sort;
        private String db_sort;

        public String getDb_sort() {
            return db_sort;
        }

        public String getServer_sort() {
            return server_sort;
        }

        FileSort(String server_sort, String db_sort) {
            this.server_sort = server_sort;
            this.db_sort = db_sort;
        }
    }

    public static enum AlbumPermissions {
        READ, WRITE
    }

    public static enum HotType {
        HOT("hot"), PROMOTED("pro");
        private String type;

        private HotType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }

    public static enum SharedMethod {
        WEIBO, // weibo
        WECHAR_MOMENT, // wechat_moment
        WECHAT_FRIEND, // wechat_friend
        QQ_SPACE, // qq_space
        PUBLIC, // public
        OTHER, // other

    }

    ;

    public final static int getBool(boolean bool) {
        if (bool) {
            return BOOLEAN_TRUE;
        }
        return BOOLEAN_FALSE;
    }

}
