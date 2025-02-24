package com.futuconnector.dto.MarketData;

import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;

import java.math.BigDecimal;

public class MarketTicker {
    private String symbol;
    private long timestamp;
    private BigDecimal price;
    private BigDecimal volume;

    public MarketTicker(String symbol, long timestamp, BigDecimal price, BigDecimal volume) {
        this.symbol = symbol;
        this.timestamp = timestamp;
        this.price = price;
        this.volume = volume;
    }

    public static MarketTicker from_ticker(String symbol, long timestamp, BigDecimal price, BigDecimal volume) {
        return new MarketTicker(symbol, timestamp, price, volume);
    }

    // Getters and setters (if needed)
    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getVolume() {
        return volume;
    }

    public void setVolume(BigDecimal volume) {
        this.volume = volume;
    }

    public Point toPoint() {
        return Point.measurement("futu_mktdata")
                .time(this.timestamp, WritePrecision.S)
                .addTag("symbol", this.symbol)
                .addField("price", this.price)
                .addField("volume", this.volume);
    }
    public Point toPoint(WritePrecision precision) {
        return Point.measurement("futu_mktdata")
                .time(this.timestamp, precision)
                .addTag("symbol", this.symbol)
                .addField("price", this.price)
                .addField("volume", this.volume);
    }
}