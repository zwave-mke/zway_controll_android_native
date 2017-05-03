package de.pathec.hubapp.model.help;

public class Help {
    String mTitle;
    String mSubtitle;
    String mText;

    public Help(String title, String subtitle, String text) {
        this.mTitle = title;
        this.mSubtitle = subtitle;
        this.mText = text;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public String getSubtitle() {
        return mSubtitle;
    }

    public void setSubtitle(String subtitle) {
        this.mSubtitle = subtitle;
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        this.mText = text;
    }
}
