package com.ostremsky.vkwalk.Authorization;

import java.io.Serializable;

/**
 * Created by DevAs on 04.07.2016.
 */
public class City implements Serializable {
    final int id;
    final String name;
    final String groupId;

    public City(int id, String name, String groupId) {
        this.id = id;
        this.name = name;
        this.groupId = groupId;
    }

    @Override
    public String toString() {
        return id + " " + name + " " + groupId + " ";
    }
}