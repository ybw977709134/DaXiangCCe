package com.daxiangce123.android.data.json;


import com.daxiangce123.android.Consts;
import com.daxiangce123.android.data.Contact;
import com.daxiangce123.android.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hansentian on 1/28/15.
 */
public class Contacts {
    private String device_id;
    private String type;

    private List<ContactBean> contacts;

    public static class ContactBean {
        private String contact;
        private String name;

        public String getContact() {
            return contact;
        }

        public void setContact(String contact) {
            this.contact = contact;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public ContactBean(String contact, String name) {
            this.contact = contact;
            this.name = name;
        }

        public ContactBean() {

        }
    }

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<ContactBean> getContacts() {
        return contacts;
    }

    public void setContacts(List<ContactBean> contacts) {
        this.contacts = contacts;
    }

    public Contacts(List<com.daxiangce123.android.data.Contact> source) {
        setType(Consts.MOBILE);
        setDevice_id(Utils.getDeviceId());
        contacts = new ArrayList<>();
        for (Contact item : source) {
            ContactBean bean = new ContactBean(item.getContact(), item.getFriend_name());
            contacts.add(bean);
        }

    }

    public Contacts() {

    }
}
