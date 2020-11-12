package com.company;

public class LocationNode {
    String source;
    String destination;
    LocationNode next;

    public LocationNode(String source, String destination) {
        this.source = source;
        this.destination = destination;
        next = null;
    }
}
