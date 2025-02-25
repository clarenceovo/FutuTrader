package com.futuconnector.dto.MarketData;

public class OrderBookEntry {
    public double price;
    public long volume;
    public int orderCount;
    private long lastUpdateTimestamp;

    public OrderBookEntry(double price, long volume, int orderCount) {
        this.price = price;
        this.volume = volume;
        this.orderCount = orderCount;
        this.lastUpdateTimestamp = System.currentTimeMillis();
    }
}