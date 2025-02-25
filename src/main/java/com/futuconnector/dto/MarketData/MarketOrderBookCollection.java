package com.futuconnector.dto.MarketData;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class MarketOrderBookCollection {
    public ConcurrentHashMap<String, MarketOrderBook> orderBooks;

    public MarketOrderBookCollection() {
        orderBooks = new ConcurrentHashMap<>();
    }


    public void addOrderBook(MarketOrderBook orderBook) {
        orderBooks.put(orderBook.symbol, orderBook);
    }

    public void updateOrderBook(MarketOrderBook orderBook) {
        orderBooks.put(orderBook.symbol, orderBook);
    }

    public MarketOrderBook getOrderBook(String symbol) {
        return orderBooks.get(symbol);
    }

    public void removeOrderBook(String symbol) {
        orderBooks.remove(symbol);
    }

    public void clear() {
        orderBooks.clear();
    }
}
