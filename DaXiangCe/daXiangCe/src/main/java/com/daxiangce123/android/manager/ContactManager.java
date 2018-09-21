package com.daxiangce123.android.manager;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.core.SingleTaskRuntime;
import com.daxiangce123.android.data.AppData;
import com.daxiangce123.android.data.ConnectInfo;
import com.daxiangce123.android.data.Contact;
import com.daxiangce123.android.data.ItemListWrapper;
import com.daxiangce123.android.helper.DBHelper;
import com.daxiangce123.android.http.ConnectBuilder;
import com.daxiangce123.android.http.Response;
import com.daxiangce123.android.parser.Parser;
import com.daxiangce123.android.ui.pages.ContactFragment;
import com.daxiangce123.android.ui.view.LoadingDialog;
import com.daxiangce123.android.util.Broadcaster;
import com.daxiangce123.android.util.DialogUtils;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.Utils;
import com.daxiangce123.android.util.ViewUtil;

import org.apache.http.HttpStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by hansentian on 1/26/15.
 * TODO this class has a very bad code style and logic should be refactor
 */
public class ContactManager {
    public static final String TAG = "ContactManager";
    private static ContactManager instance;
    public List<Contact> serverContacts = Collections.synchronizedList(new LinkedList<Contact>());
    public List<Contact> localContacts = Collections.synchronizedList(new LinkedList<Contact>());
    public List<Contact> systemContact = Collections.synchronizedList(new LinkedList<Contact>());
    public List<Contact> originContact = Collections.synchronizedList(new LinkedList<Contact>());

    public List<Contact> noMatchContact = new LinkedList<>();
    public List<Contact> matchContact = new LinkedList<>();

    private boolean showNoContactReadAleart = false;

    private DBHelper db = App.getDBHelper();

    private ContactFragment contactFragment;

    /**
     * contact in local but not in server
     */
//    private List<Contact> displayContacts = Collections.synchronizedList(new LinkedList<Contact>());
    public List<Contact> getNoMatchContact() {
        return noMatchContact;
    }

    public List<Contact> getMatchContact() {
        return matchContact;
    }

    public static ContactManager getInstance() {
        if (instance == null) {
            instance = new ContactManager();
        }
        return instance;
    }

    private ContactManager() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Consts.GET_CONTACTS);
        intentFilter.addAction(Consts.CREATE_CONTACT);
        intentFilter.addAction(Consts.DELETE_ALBUM);
        Broadcaster.registerReceiver(receiver, intentFilter);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                LogUtil.v(TAG, "onReceive...   " + action);
                switch (action) {
                    case Consts.GET_CONTACTS:
                        onGetFromServer(intent);
                        break;
                    case Consts.CREATE_CONTACT:
                        onCreateContact(intent);
                        break;
                    case Consts.DELETE_CONTACT:
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    /**
     * sync local contact to server
     * attention: this method is not running in the main thread!
     */
    public void syncToServer(boolean showAlert) {
        if (App.DEBUG) {
            LogUtil.v(TAG, "syncToServer...");
        }
        if (App.getUserInfo() == null || (!App.getUserInfo().isBindMobile())) {
            return;
        }
        if (AppData.hasUserAgreeToReadContact()) {
            syncToServerImp();
        } else if (showAlert) {
            if (AppData.getReadContactPopupTime() > 2) {
            } else {
                showReadContactAlert();
            }
        }
    }

    /**
     * sync local contact to server
     * attention: this method is not running in the main thread!
     */
    public void syncToServer2(boolean showAlert, ContactFragment pFragment) {
        syncToServer(false);
        showNoContactReadAleart = showAlert;
        contactFragment = pFragment;
        LoadingDialog.show(R.string.loading);
    }

    public void showNoContactReadAlert() {
        if (contactFragment != null && contactFragment.isVisible()) {
            if (localContacts.size() == 0 && systemContact.size() == 0) {
                contactFragment.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        contactFragment.showEmptyView(true);
                    }
                });
            } else if (originContact.size() == 0 && localContacts.size() != 0) {
                if (showNoContactReadAleart) {
                    ViewUtil.aleartMessage(R.string.cant_get_contact, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }, contactFragment.getActivity());
                }
                showNoContactReadAleart = false;
            } else {
                contactFragment.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        contactFragment.showEmptyView(false);
                    }
                });
            }
        }
    }

    public void syncToServerImp() {
        if (AppData.isLogin()) {
            SingleTaskRuntime.instance().run(new Runnable() {
                @Override
                public void run() {
                    fetchContactInformation();
                    fetchDb();
                    deleteContacts();
                    parpareShowList();
                    fetchServer();
                }
            });
        }
    }

    /**
     * read from local database
     */
    private void fetchDb() {
        DBHelper dbHelper = App.getDBHelper();
        LinkedList<Contact> contacts = dbHelper.getList(Contact.EMPTY);
        if (contacts != null) {
            synchronized (localContacts) {
                localContacts.clear();
                localContacts.addAll(contacts);
            }
        }
        for (Contact item : localContacts) {
            Log.v(TAG, item.toString());
        }
    }


    private void fetchServer() {
        ConnectBuilder.getContactList();
    }

    private void parpareShowList() {
        boolean readSuccess = true;
        if (systemContact.size() == 0) {
            readSuccess = false;
        }
        synchronized (serverContacts) {
            if (localContacts.size() != 0) {
                serverContacts.clear();
                //TODO merge list
                for (Contact dbItem : localContacts) {
                    if (dbItem.isRegister()) {
                        serverContacts.add(dbItem);
                        Contact registered = null;
                        //删除 从系统读出的已经注册的contact
                        for (Contact contact : systemContact) {
                            if (contact.getContact().equals(dbItem.getContact())) {
                                registered = contact;
                                break;
                            }
                        }
                        if (registered != null) {
                            systemContact.remove(registered);
                        }
                    } else if (!readSuccess) {
                        systemContact.add(dbItem);

                    }
                }
                Log.v(TAG, "parpareShowList serverContacts");
            }

            refreshShowList();
        }
    }

    private void deleteContacts() {
        if (originContact.size() != 0 && (localContacts.size() != 0)) {
            Iterator<Contact> iterator = localContacts.iterator();
            while (iterator.hasNext()) {
                Contact dbItem = iterator.next();
                boolean findInSystem = false;
                for (Contact systemItem : originContact) {
                    if (dbItem.getContact().equals(systemItem.getContact())) {
                        findInSystem = true;
                        break;
                    }
                }
                if (!findInSystem) {
                    iterator.remove();
                    db.delete(dbItem);
                    ConnectBuilder.deleteContact(dbItem);
                }
            }
        }


    }

    /**
     * this must be run in the thread
     */
    private void refreshShowList() {
        synchronized (serverContacts) {
            Utils.sordContast(serverContacts);
        }
        synchronized (systemContact) {
            Utils.sordContast(systemContact);
        }
        if (contactFragment != null) {
            contactFragment.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (contactFragment.isVisible()) {
                        matchContact.clear();
                        matchContact.addAll(serverContacts);
                        for (Contact item : noMatchContact) {
                            Log.v(TAG, item.toString());
                        }
                        noMatchContact.clear();
                        noMatchContact.addAll(systemContact);
                        contactFragment.contactAdapter.notifyDataSetChanged();
                        LoadingDialog.dismiss();
                        showNoContactReadAlert();
                    }

                }
            });

        }
    }


    /**
     * read from system contact
     */
    private void fetchContactInformation() {
        List<Contact> items = new ArrayList<>();
        ContentResolver cr = App.getAppContext().getContentResolver();
        HashMap<String, ArrayList<String>> hashMap = new HashMap<>();
        Cursor phone = cr.query(CommonDataKinds.Phone.CONTENT_URI,
                new String[]{
                        CommonDataKinds.Phone.CONTACT_ID,
                        CommonDataKinds.Phone.DISPLAY_NAME,
                        CommonDataKinds.Phone.NUMBER,
                        CommonDataKinds.Phone.DATA1, CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI
                }, CommonDataKinds.Phone.TYPE + "=" + CommonDataKinds.Phone.TYPE_MOBILE
                , null, "display_name COLLATE LOCALIZED ASC");
        while (phone.moveToNext()) {
            String contactId = phone.getString(phone.getColumnIndex(CommonDataKinds.Phone.CONTACT_ID));
            String displayName = phone.getString(phone.getColumnIndex(CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            String photoUri = phone.getString(phone.getColumnIndex(CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI));
            ArrayList<String> ad = hashMap.get(contactId);
            Contact contact = new Contact(displayName, phoneNumber);
            contact.setPhotoUri(photoUri);
            items.add(contact);
        }
        phone.close();
        synchronized (systemContact) {
            systemContact.clear();
            systemContact.addAll(items);
        }
        originContact.clear();
        originContact.addAll(items);
        if (Utils.isEmpty(systemContact)) {
            showNoContactReadAlert();
        }
    }

    public boolean isSync() {
        return false;
    }

    private void showReadContactAlert() {
        AlertDialog.Builder dialog = DialogUtils.create();
        dialog.setMessage(R.string.look_for_who_are_using);
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    AppData.sethasUserAgreeToReadContact(true);
                    syncToServerImp();
                } else {
                    AppData.setReadContactPopupTime(AppData.getReadContactPopupTime() + 1);
                }
                dialog.cancel();
            }
        };
        dialog.setPositiveButton(R.string.all_right, listener);
        dialog.setNegativeButton(R.string.not_now, listener);
        AlertDialog d = dialog.show();
        TextView msg = (TextView) d.findViewById(android.R.id.message);
        msg.setGravity(Gravity.CENTER);
    }


    private void onGetFromServer(Intent intent) {
        Response response = intent.getParcelableExtra(Consts.RESPONSE);
        ConnectInfo info = intent.getParcelableExtra(Consts.REQUEST);
        if (App.DEBUG) {
            Log.v(TAG, response.getContent());
        }
        if (originContact.size() != 0) {
            if ((HttpStatus.SC_OK == response.getStatusCode()) && (!TextUtils.isEmpty(response.getContent()))) {
                ItemListWrapper<Contact> list = Parser.parseContactList(response.getContent());
                synchronized (serverContacts) {
                    //if not read from lo
                    if (Integer.valueOf(info.getTag()) == 0) {
                        serverContacts.clear();
                    }
                    if (!Utils.isEmpty(list.getItemList())) {
                        serverContacts.addAll(list.getItemList());
                    }
                    if (list.isHasMore()) {
                        ConnectBuilder.getContactList(Integer.valueOf(info.getTag()) + list.getItemList().size(), 200);
                    } else {
                        uploadContact();
                    }
                }

            }
        }
    }

    private void onCreateContact(Intent intent) {
        Response response = intent.getParcelableExtra(Consts.RESPONSE);
        ConnectInfo info = intent.getParcelableExtra(Consts.REQUEST);
        if (App.DEBUG) {
//            LogUtil.v(TAG, "onCreateContact " + response.getContent());
        }
        if ((HttpStatus.SC_OK == response.getStatusCode())) {
            ItemListWrapper<Contact> list = Parser.parseContactList(response.getContent());
            if (list == null) return;
            final List<Contact> createList = list.getItemList();
            SingleTaskRuntime.instance().run(new Runnable() {
                @Override
                public void run() {
                    for (Contact contact : originContact) {
                        for (Contact create : createList) {
                            if (contact.getContact().equals(create.getContact())) {
                                create.setPhotoUri(contact.getPhotoUri());
                                break;
                            }
                        }
                    }
                }
            });
//            if (App.DEBUG) {
//                LogUtil.v(TAG, "onCreateContact return 200");
//            }
        }
    }

    /**
     * upload the contact that not upload and not in sererList yet
     */
    private void uploadContact() {
        SingleTaskRuntime.instance().run(new Runnable() {
            @Override
            public void run() {
                if (originContact.size() != 0) {
                    List<Contact> needToUpload = getUploadList();
                    ConnectBuilder.createContacts(needToUpload);
                    if (App.DEBUG) {
//                        Log.v(TAG, "uploadContact refreshShowList");
                    }
                    refreshShowList();
                }
            }
        });
    }

    /**
     * compare the list that in db and local return
     *
     * @return
     */
    private List<Contact> getUploadList() {
        if (originContact.size() == 0) {
            return null;
        }
        List<Contact> tempSystemList = Collections.synchronizedList(new LinkedList<Contact>());
        //copy to temp list
        for (Contact item : originContact) {
            tempSystemList.add(item);
        }
        synchronized (serverContacts) {
            if (originContact.size() != 0) {
                Iterator<Contact> iterator = serverContacts.iterator();
                while (iterator.hasNext()) {
                    Contact serverItem = iterator.next();
                    boolean findInSystem = false;
                    for (Contact systemItem : originContact) {
                        if (serverItem.getContact().equals(systemItem.getContact())) {
                            findInSystem = true;
                            break;
                        }
                    }
                    if (!findInSystem) {
                        iterator.remove();
                    }
                }
            }
        }
        //delete  item which is in server from showList
        synchronized (serverContacts) {
            Iterator<Contact> iterator = tempSystemList.iterator();
            while (iterator.hasNext()) {
                Contact system = iterator.next();
                boolean deleted = false;
                for (Contact serverItem : serverContacts) {
                    if (system.getContact().equals(serverItem.getContact())) {
                        iterator.remove();
                        //swap system list
                        systemContact.remove(system);
                        serverItem.setPhotoUri(system.getPhotoUri());
                        serverItem.setFriend_name(system.getFriend_name());
                        deleted = true;
                        break;
                    }
                }
                if (!deleted) {
                    for (Contact dbItem : localContacts) {
                        if (system.getContact().equals(dbItem.getContact())) {
                            iterator.remove();

                            break;
                        }
                    }
                }
            }
        }
//        LogUtil.v(TAG, " getUploadList insert into db....");
        db.insert(serverContacts);
        db.insert(systemContact);
        return tempSystemList;
    }
}
