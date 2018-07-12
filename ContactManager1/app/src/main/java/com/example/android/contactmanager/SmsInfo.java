package com.example.android.contactmanager;

/**
 * Created by SONY on 2015/4/3.
 */
public class SmsInfo {
    private String id;
    private String linkman;//联系人
    private String number;//联系号码
    private String content;
    private long date;
    private int read;//0未读 1已读
    private int type;//联系类型 1接收 2发送  3草稿

    public SmsInfo(){}
    public SmsInfo(String id, String linkman, String number, String content, long date,
                   int type) {
        this.id = id;
        this.linkman = linkman;
        this.number = number;
        this.content = content;
        this.date = date;
        this.type = type;
    }

    public SmsInfo(String id, String linkman, String number, String content, long date,
                   int read, int type) {
        this.id = id;
        this.linkman = linkman;
        this.number = number;
        this.content = content;
        this.date = date;
        this.read = read;
        this.type = type;
    }
    public void setID(String str) {
        this.id = str;
    }
    public String getID() { return id; }
    public int getRead() {
        return read;
    }
    public void setRead(int read) {
        this.read = read;
    }
    public String getLinkman() {
        return linkman;
    }
    public void setLinkman(String linkman) {
        this.linkman = linkman;
    }
    public String getNumber() {
        return number;
    }
    public void setNumber(String number) {
        this.number = number;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }

    public long getDate() {
        return date;
    }
    public void setDate(long date) {
        this.date = date;
    }
    public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
    }
    @Override
    public String toString() {
        return "SmsInfo [linkman=" + linkman + ", number=" + number
                + ", content=" + content + ", date=" + date + ", type=" + type
                + "]";
    }

}
