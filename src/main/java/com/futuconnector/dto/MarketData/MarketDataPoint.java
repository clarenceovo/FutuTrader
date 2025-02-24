package com.futuconnector.dto.MarketData;

import com.influxdb.client.write.Point;

import java.math.BigDecimal;
import java.time.Instant;

public record MarketDataPoint(
        String symbol,
        long timestamp,
        BigDecimal open,
        BigDecimal high,
        BigDecimal low,
        BigDecimal close,
        BigDecimal volume,
        MarketDataIntervalEnum interval
) {
    public Point toPoint() {

        return Point.measurement("futu_mktdata")
                .time(Instant.ofEpochMilli(this.timestamp), com.influxdb.client.domain.WritePrecision.MS)
                .addTag("symbol", this.symbol)
                .addTag("interval", this.interval.toString())
                .addField("open", this.open)
                .addField("high", this.high)
                .addField("low", this.low)
                .addField("close", this.close)
                .addField("volume", this.volume);
    }
    public Point toPoint(String measurement) {

        return Point.measurement(measurement)
                .time(Instant.ofEpochMilli(this.timestamp), com.influxdb.client.domain.WritePrecision.MS)
                .addTag("symbol", this.symbol)
                .addTag("interval", this.interval.toString())
                .addField("open", this.open)
                .addField("high", this.high)
                .addField("low", this.low)
                .addField("close", this.close)
                .addField("volume", this.volume);
    }
}