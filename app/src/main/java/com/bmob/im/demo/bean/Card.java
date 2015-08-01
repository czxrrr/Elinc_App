package com.bmob.im.demo.bean;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobRelation;

/**
 * Created by HUBIN on 2015/7/31.
 */
public class Card extends BmobObject {
    private User Cardsender;
    private String claim;
    private String goalContent;
    private BmobRelation likedBy;

    public BmobRelation getLikedBy() {
        return likedBy;
    }

    public void setLikedBy(BmobRelation likedBy) {
        this.likedBy = likedBy;
    }

    public User getCardsender() {
        return Cardsender;
    }

    public void setCardsender(User cardsender) {
        Cardsender = cardsender;
    }

    public String getClaim() {
        return claim;
    }

    public void setClaim(String claim) {
        this.claim = claim;
    }

    public String getGoalContent() {
        return goalContent;
    }

    public void setGoalContent(String goalContent) {
        this.goalContent = goalContent;
    }

    public void setlikedBy(BmobRelation likedBy) {
        this.likedBy = likedBy;
    }
}
