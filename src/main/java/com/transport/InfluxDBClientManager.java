package com.transport;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApi;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxTable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class InfluxDBClientManager {
    private static final Logger logger = LogManager.getLogger("InfluxDBClientManager");
    private static HashMap<String,InfluxDBClientManager> instance = new HashMap<>();
    private static int WRITE_INTERVAL_MS ;
    private final InfluxDBClient influxDBClient;
    private List<Point> bufferList = Collections.synchronizedList(new ArrayList<Point>());
    private long lastWriteTime = System.currentTimeMillis();
    private final String url;
    private final String org;
    private final String token;
    private final String bucket;

    public InfluxDBClientManager(String url, String token, String org,String bucket) {
        this.url = url;
        this.org = org;
        this.token = token;
        this.bucket = bucket;
        logger.info("Creating InfluxDB client... {} , {} , {} , {}", url, token, org, bucket);
        if (System.getenv("WRITE_INTERVAL_MS") != null) {
            WRITE_INTERVAL_MS = Integer.parseInt(System.getenv("WRITE_INTERVAL_MS"));
        } else {
            WRITE_INTERVAL_MS = 1000;
        }
        this.influxDBClient = InfluxDBClientFactory.create(this.url, this.token.toCharArray(), this.org,this.bucket);
    }

    public static synchronized InfluxDBClientManager getInstance(String url, String token, String org, String bucket) {
        return instance.computeIfAbsent(bucket, k -> new InfluxDBClientManager(url, token, org, bucket));
    }

    public void writeData(Point point) {
        bufferList.add(point);
        if (System.currentTimeMillis() - lastWriteTime > WRITE_INTERVAL_MS) { // write data every 500ms
            logger.debug("Writing data to InfluxDB...");
            writeData();
        }
    }

    private void writeData() {
        List<Point> points = new ArrayList<>(bufferList);
        try (WriteApi writeApi = influxDBClient.getWriteApi()) {
            writeApi.writePoints(points);
            bufferList.clear();
        } catch (Exception e) {
            logger.error("Error writing data to InfluxDB: {}", e.getMessage(), e);
        }
    }

    public List<FluxTable> queryData(String fluxQuery) {
        QueryApi queryApi = influxDBClient.getQueryApi();
        return queryApi.query(fluxQuery, org);
    }

    public void close() {
        influxDBClient.close();
        logger.info("InfluxDB client closed");
    }
}