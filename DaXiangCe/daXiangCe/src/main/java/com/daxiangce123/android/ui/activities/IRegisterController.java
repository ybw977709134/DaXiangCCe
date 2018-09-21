package com.daxiangce123.android.ui.activities;

import com.daxiangce123.R;
import com.daxiangce123.android.Consts;

/**
 * Created by hansentian on 12/25/14.
 */
public interface IRegisterController {

    public void showSignin(RegisterBundle bundle);

    public void showRegister(RegisterBundle bundle);

    public void showConfirmationReceiver(RegisterBundle bundle);

    public void showSubmiter(RegisterBundle bundle);

    public void mobileLogin(RegisterBundle bundle);

    public enum RegisterType {
        passwordRecovery, register, bind, login;
    }

    public class RegisterBundle {
        public Consts.PURPOZE purpoze;
        public RegisterType registerType;
        public String mobile;
        public String oldMobile;
        public String confirmation;
        public String password;
        public boolean showLogin = true;
        public int title;
        public boolean isNew;

        public void setRegisterType(RegisterType registerType) {
            this.registerType = registerType;
            if (registerType == RegisterType.passwordRecovery) {
                purpoze = Consts.PURPOZE.password;
                title = R.string.find_password;
            } else if (registerType == RegisterType.register) {
                purpoze = Consts.PURPOZE.registration;
                title = R.string.register;
            } else if (registerType == RegisterType.bind) {
                showLogin = false;
                purpoze = Consts.PURPOZE.registration;
                title = R.string.binding_phone_number;
            }
        }

        public void newUserBindPhone(boolean isNew){
            this.isNew = isNew;
        }
    }
}
