package com.daxiangce123.android.data.json;

import com.daxiangce123.android.data.Contact;

import java.util.ArrayList;

/**
 * Created by hansentian on 1/28/15.
 */
public class CreateContactsWrapper {

    ArrayList<Contact> contacts;

    public ArrayList<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(ArrayList<Contact> contacts) {
        this.contacts = contacts;
    }
}
