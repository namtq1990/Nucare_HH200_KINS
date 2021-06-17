package android.HH100.Dialog;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.widget.Toast;

/**
 * Created by inseon.ahn on 2019-01-23.
 */
public class PwdEditTextPreference extends EditTextPreference {
    final static String PWD_SALT = "8L9f7BL5Ot5jCJ0Hu7iO";

    public PwdEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public PwdEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public PwdEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PwdEditTextPreference(Context context) {
        super(context);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setText(String text) {
        String pwdHash = super.getText();
        if (text != null && text.trim().length() >= 4) {
            // Si on récupère l'ancien mot de passe alors ne pas le hacher
            if (pwdHash == null || pwdHash.trim().length() == 0 || pwdHash.equals(text)) {
                super.setText(text);
            }
            // Si on modifie le mot de passe alors le hacher
            else if (!pwdHash.equals(text)) {
                pwdHash = "dddd";
                super.setText(pwdHash);
            }
        }
    }
}