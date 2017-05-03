package de.pathec.hubapp.util;

import android.app.Activity;
import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import de.pathec.hubapp.R;

public class Validator {

    Context mContext;
    Activity mActivity;

    public Validator(Context context, Activity activity) {
        this.mContext = context;
        this.mActivity = activity;
    }

    public boolean validateBlank(Boolean requestFocus, TextInputLayout layout, EditText editText, int labelRes) {
        if (requestFocus) {
            if (editText.getText().toString().trim().isEmpty()) {
                layout.setError(mContext.getString(R.string.validation_blank, mContext.getString(labelRes)));
                requestFocus(editText);
                return false;
            } else {
                layout.setErrorEnabled(false);
            }
            return true;
        } else {
            return !editText.getText().toString().trim().isEmpty();
        }
    }

    public boolean validateNumber(Boolean requestFocus, TextInputLayout layout, EditText editText, int labelRes) {
        if (requestFocus) {
            if (editText.getText().toString().trim().isEmpty()) {
                layout.setError(mContext.getString(R.string.validation_blank, mContext.getString(labelRes)));
                requestFocus(editText);
                return false;
            } else {
                try {
                    Integer.parseInt(editText.getText().toString());
                } catch (NumberFormatException nfe) {
                    layout.setError(mContext.getString(R.string.validation_number, mContext.getString(labelRes)));
                    requestFocus(editText);
                    return false;
                }
                layout.setErrorEnabled(false);
            }
            return true;
        } else {
            if (editText.getText().toString().trim().isEmpty()) {
                return false;
            } else {
                try {
                    Integer.parseInt(editText.getText().toString());
                } catch (NumberFormatException nfe) {
                    return false;
                }
                return true;
            }
        }
    }

    public boolean validateEmail(Boolean requestFocus, TextInputLayout layout, EditText editText, int labelRes) {
        if (requestFocus) {
            if (editText.getText().toString().trim().isEmpty()) {
                layout.setError(mContext.getString(R.string.validation_blank, mContext.getString(labelRes)));
                requestFocus(editText);
                return false;
            } else {
                if (!editText.getText().toString().matches(Params.EMAIL_PATTERN)) {
                    layout.setError(mContext.getString(R.string.validation_invalid, mContext.getString(labelRes)));
                    requestFocus(editText);
                    return false;
                }
                layout.setErrorEnabled(false);
                requestFocus(editText);
            }

            return true;
        } else {
            return (!editText.getText().toString().trim().isEmpty()
                    && editText.getText().toString().matches(Params.EMAIL_PATTERN));
        }
    }

    public boolean validatePassword(TextInputLayout layout, EditText editText, TextInputLayout layoutRetype, EditText editTextRetype, int labelRes) {
        if (editText.getText().toString().trim().length() < 6) {
            layout.setError(mContext.getString(R.string.validation_min, mContext.getString(labelRes), "6"));
            return false;
        } else if (editText.getText().toString().trim().length() < 6) {
            layoutRetype.setError(mContext.getString(R.string.validation_min, mContext.getString(labelRes), "6"));
            return false;
        } else {
            if(!editText.getText().toString().equals(editTextRetype.getText().toString())) {
                layout.setError(mContext.getString(R.string.validation_match, mContext.getString(labelRes)));
                layoutRetype.setError(mContext.getString(R.string.validation_match, mContext.getString(labelRes)));
                return false;
            } else {
                layout.setErrorEnabled(false);
                layoutRetype.setErrorEnabled(false);
            }
        }
        return true;
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }
}
