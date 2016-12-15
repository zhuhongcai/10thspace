package com.tenth.space.DB.entity;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
// KEEP INCLUDES END

import java.io.Serializable;

/**
 * Entity mapped to table RequesterInfo.
 */
public class RequesterEntity implements Serializable{

    private Long id;
    private String avatar_url;
    private Long created;
    private Boolean isRead;
    /** Not-null value. */
    private String nick_name;
    private int fromUserId;
    private String addition_msg;
    private int agree_states;

    // KEEP FIELDS - put your custom fields here
    // KEEP FIELDS END

    public RequesterEntity() {
    }

    public RequesterEntity(Long id) {
        this.id = id;
    }

    public RequesterEntity(Long id, String avatar_url, Long created, Boolean isRead, String nick_name, int fromUserId, String addition_msg, int agree_states) {
        this.id = id;
        this.avatar_url = avatar_url;
        this.created = created;
        this.isRead = isRead;
        this.nick_name = nick_name;
        this.fromUserId = fromUserId;
        this.addition_msg = addition_msg;
        this.agree_states = agree_states;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAvatar_url() {
        return avatar_url;
    }

    public void setAvatar_url(String avatar_url) {
        this.avatar_url = avatar_url;
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    /** Not-null value. */
    public String getNick_name() {
        return nick_name;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setNick_name(String nick_name) {
        this.nick_name = nick_name;
    }

    public int getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(int fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getAddition_msg() {
        return addition_msg;
    }

    public void setAddition_msg(String addition_msg) {
        this.addition_msg = addition_msg;
    }

    public int getAgree_states() {
        return agree_states;
    }

    public void setAgree_states(int agree_states) {
        this.agree_states = agree_states;
    }

    // KEEP METHODS - put your custom methods here
    // KEEP METHODS END

}
