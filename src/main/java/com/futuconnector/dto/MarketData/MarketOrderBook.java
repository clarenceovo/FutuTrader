package com.futuconnector.dto.MarketData;

import com.futu.openapi.pb.QotCommon;

import java.util.List;
import java.util.TreeMap;

public class MarketOrderBook {
    public String symbol;
    private TreeMap<Double, OrderBookEntry> askOrders;
    private TreeMap<Double, OrderBookEntry> bidOrders;
    private long lastUpdateTimestamp;

    public MarketOrderBook(String symbol) {
        this.symbol = symbol;
        askOrders = new TreeMap<>();
        bidOrders = new TreeMap<>();
        lastUpdateTimestamp = System.currentTimeMillis();
    }

    public MarketOrderBook(String symbol, long timestamp) {
        this.symbol = symbol;
        askOrders = new TreeMap<>();
        bidOrders = new TreeMap<>();
        lastUpdateTimestamp = timestamp;
    }

    private void updateTimestamp() {
        lastUpdateTimestamp = System.currentTimeMillis();
    }

    public void addAskOrder(double price, OrderBookEntry entry) {

        askOrders.put(price, entry);
    }

    public void addBidOrder(double price, OrderBookEntry entry) {
        bidOrders.put(price, entry);
    }

    public OrderBookEntry getAskOrder(double price) {
        return askOrders.get(price);
    }

    public OrderBookEntry getBidOrder(double price) {
        return bidOrders.get(price);
    }

    public void removeAskOrder(double price) {
        askOrders.remove(price);
    }

    public void removeBidOrder(double price) {
        bidOrders.remove(price);
    }

    public TreeMap<Double, OrderBookEntry> getAskOrders() {
        return askOrders;
    }

    public TreeMap<Double, OrderBookEntry> getBidOrders() {
        return bidOrders;
    }

    public double getMidPrice() {
        if (bidOrders.isEmpty() || askOrders.isEmpty()) {
            return 0;
        }
        return (bidOrders.lastKey() + askOrders.firstKey()) / 2;
    }

    public double getSpread() {
        if (bidOrders.isEmpty() || askOrders.isEmpty()) {
            return 0;
        }
        return askOrders.firstKey() - bidOrders.lastKey();
    }

    public int getAskOrderCount() {
        int count = 0;
        for (OrderBookEntry entry : askOrders.values()) {
            count += entry.orderCount;
        }
        return count;
    }

    public int getBidOrderCount() {
        int count = 0;
        for (OrderBookEntry entry : bidOrders.values()) {
            count += entry.orderCount;
        }
        return count;
    }

    public long getBidDepth() {
        long depth = 0;
        for (OrderBookEntry entry : bidOrders.values()) {
            depth += entry.volume;
        }
        return depth;
    }

    public long getAskDepth() {
        long depth = 0;
        for (OrderBookEntry entry : askOrders.values()) {
            depth += entry.volume;
        }
        return depth;
    }


    public static MarketOrderBook fromFUTUOrderBook(String symbol, long timestamp, List<QotCommon.OrderBook> bidList, List<QotCommon.OrderBook> askList) {
        MarketOrderBook orderBook = new MarketOrderBook(symbol, timestamp);
        for (QotCommon.OrderBook order : bidList) {
            orderBook.addBidOrder(order.getPrice(), new OrderBookEntry(order.getPrice(), order.getVolume(), order.getOrederCount()));
        }
        for (QotCommon.OrderBook order : askList) {
            orderBook.addAskOrder(order.getPrice(), new OrderBookEntry(order.getPrice(), order.getVolume(), order.getOrederCount()));
        }
        return orderBook;
    }
}
