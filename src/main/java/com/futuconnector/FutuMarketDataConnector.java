package com.futuconnector;

import com.futu.openapi.FTAPI_Conn;
import com.futu.openapi.FTAPI_Conn_Qot;
import com.futu.openapi.FTSPI_Conn;
import com.futu.openapi.FTSPI_Qot;
import com.futu.openapi.pb.QotGetSubInfo;
import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;

import com.google.protobuf.util.JsonFormat;

public class FutuMarketDataConnector implements FTSPI_Qot, FTSPI_Conn {
    private static final Logger logger = LogManager.getLogger("FutuMarketDataConnector");
    FTAPI_Conn_Qot quoter = new FTAPI_Conn_Qot();
    private static FutuMarketDataConnector instance;
    private boolean isActive;
    URI endpoint;
    short port;


    public FutuMarketDataConnector(String url) {
        quoter.setClientInfo("FutuMarketData", 1);
        quoter.setConnSpi(this);
        try {
            this.endpoint = new URI(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public FutuMarketDataConnector() {
        quoter.setClientInfo("FutuMarketData", 1);
        quoter.setConnSpi(this);
        try {
            this.endpoint = new URI("127.0.0.1");
            this.port = (short) 11111;
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public void subscribe(String symbol) {

    }

    public void getSubscriptionInfo() {
        QotGetSubInfo.C2S c2s = QotGetSubInfo.C2S.newBuilder()
                .build();
        QotGetSubInfo.Request req = QotGetSubInfo.Request.newBuilder().setC2S(c2s).build();
        int seqNo = quoter.getSubInfo(req);
        logger.info("GetSubInfo: {}\n", seqNo);

    }

    @Override
    public void onDisconnect(FTAPI_Conn client, long errCode) {
        logger.error("FutuMarketDataConnector onDisConnect: %d\n", errCode);
    }

    @Override
    public void onReply_GetSubInfo(FTAPI_Conn client, int nSerialNo, QotGetSubInfo.Response rsp) {
        if (rsp.getRetType() != 0) {
            System.out.printf("QotGetSubInfo failed: %s\n", rsp.getRetMsg());
        } else {
            try {
                String json = JsonFormat.printer().print(rsp);
                System.out.printf("Receive QotGetSubInfo: %s\n", json);
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
    }
    public boolean isActive() {
        return isActive;
    }

    public void start() {
        logger.info("Attempting to connect to Futu Market Data server...");
        try {
            quoter.initConnect(this.endpoint.toString(), this.port, false);
            logger.info("Successfully connected to Futu Market Data server.");
            this.isActive = true;

        } catch (Exception e) {
            logger.error("Exception during Futu Market Data connection: {}", e.getMessage(), e);
        }
    }

}
