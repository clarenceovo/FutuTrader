package com.futuconnector;

import com.futu.openapi.*;
import com.futu.openapi.pb.*;


import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import com.google.protobuf.util.JsonFormat;

public class FutuTradingConnector implements FTSPI_Trd, FTSPI_Conn {
    private static final Logger logger = LogManager.getLogger("FutuTradingConnector");
    private static FutuTradingConnector instance;
    private boolean isActive;
    private short port;
    private boolean isLiveOrder = false;
    private int futuId;
    URI endpoint;
    FTAPI_Conn_Trd trading_conn = new FTAPI_Conn_Trd();
    HashMap<Long, TrdCommon.TrdAcc> acctBook = new HashMap<>();
    HashMap<String, TrdCommon.Position> positionBook = new HashMap<>();
    HashMap<Long, TrdCommon.Funds> fundBook = new HashMap<>();

    private FutuTradingConnector(String url, short port, int futuId) {
        trading_conn.setClientInfo("FutuTrading", 1);
        trading_conn.setConnSpi(this);
        trading_conn.setTrdSpi(this);
        try {

            this.endpoint = new URI(url);
            this.port = port;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //for local
    private FutuTradingConnector(int futuId) {
        this.futuId = futuId;
        trading_conn.setClientInfo("FutuTrading", 1);
        trading_conn.setConnSpi(this);
        trading_conn.setTrdSpi(this);
        try {

            this.endpoint = new URI("127.0.0.1");
            this.port = (short) 11111;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized FutuTradingConnector getInstance(String url, short port, int futuId) {
        if (instance == null) {
            logger.info("Creating FutuTradingConnector. Host: {}, Port: {}, FutuID: {}", url, port, futuId);
            instance = new FutuTradingConnector(url, port, futuId);
        }
        return instance;
    }

    public static synchronized FutuTradingConnector getInstance(int futuId) {
        if (instance == null) {
            logger.info("Creating FutuTradingConnector...");
            instance = new FutuTradingConnector(futuId);
        }
        return instance;
    }


    public HashMap<String, TrdCommon.Position> getPosition() {
        return positionBook;
    }

    @Override
    public void onDisconnect(FTAPI_Conn client, long errCode) {
        logger.fatal("FutuTradingConnector onDisConnect: {}\n", errCode);
    }


    @Override
    public void onInitConnect(FTAPI_Conn client, long errCode, String desc) {
        logger.info("Trd onInitConnect: ret={} desc={} connID={}\n", errCode, desc, client.getConnectID());
        getAcctList();
        getFutAcctList();

    }

    public void getFunds(long accID) {
        TrdCommon.TrdHeader header = TrdCommon.TrdHeader.newBuilder()
                .setAccID(accID)
                .setTrdEnv(TrdCommon.TrdEnv.TrdEnv_Real_VALUE)
                .setTrdMarket(TrdCommon.TrdMarket.TrdMarket_HK_VALUE)
                .build();
        TrdGetFunds.C2S c2s = TrdGetFunds.C2S.newBuilder()
                .setHeader(header)
                .setCurrency(TrdCommon.Currency.Currency_HKD_VALUE)
                .build();
        TrdGetFunds.Request req = TrdGetFunds.Request.newBuilder().setC2S(c2s).build();
        int serialNo = trading_conn.getFunds(req);
        logger.debug("Send TrdGetFunds: %d\n", serialNo);
    }

    public void getPosition(long accID) {
        TrdCommon.TrdHeader header = TrdCommon.TrdHeader.newBuilder()
                .setAccID(accID)
                .setTrdEnv(TrdCommon.TrdEnv.TrdEnv_Real_VALUE)
                .setTrdMarket(TrdCommon.TrdMarket.TrdMarket_HK_VALUE)
                .build();
        TrdGetPositionList.C2S c2s = TrdGetPositionList.C2S.newBuilder()
                .setHeader(header)
                .build();
        TrdGetPositionList.Request req = TrdGetPositionList.Request.newBuilder().setC2S(c2s).build();
        int serialNo = trading_conn.getPositionList(req);
        logger.debug("Send TrdGetPositionList: %d\n", serialNo);
    }

    public void getMarginRatio(long accID, String code) {
        TrdCommon.TrdHeader header = TrdCommon.TrdHeader.newBuilder()
                .setAccID(accID)
                .setTrdEnv(TrdCommon.TrdEnv.TrdEnv_Real_VALUE)
                .setTrdMarket(TrdCommon.TrdMarket.TrdMarket_HK_VALUE)
                .build();
        QotCommon.Security security = QotCommon.Security.newBuilder()
                .setCode(code)
                .setMarket(QotCommon.QotMarket.QotMarket_HK_Security_VALUE)
                .build();
        TrdGetMarginRatio.C2S c2s = TrdGetMarginRatio.C2S.newBuilder()
                .setHeader(header)
                .addSecurityList(security)
                .build();
        TrdGetMarginRatio.Request req = TrdGetMarginRatio.Request.newBuilder().setC2S(c2s).build();
        int seqNo = trading_conn.getMarginRatio(req);
        System.out.printf("Send TrdGetMarginRatio: %d\n", seqNo);
    }

    @Override
    public void onReply_GetFunds(FTAPI_Conn client, int nSerialNo, TrdGetFunds.Response rsp) {
        if (rsp.getRetType() != 0) {
            logger.error("TrdGetFunds failed:" + rsp.getRetMsg());
        } else {
            try {
                String json = JsonFormat.printer().print(rsp);
                logger.debug("Receive TrdGetFunds: {}\n", json);
                if (rsp.getS2C().getFunds() == null)
                    return;
                fundBook.put(rsp.getS2C().getHeader().getAccID(), rsp.getS2C().getFunds());

            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
    }


    public void getAcctList() {
        TrdGetAccList.C2S c2s = TrdGetAccList.C2S.newBuilder().setUserID(this.futuId)
                .setTrdCategory(TrdCommon.TrdCategory.TrdCategory_Security_VALUE)
                .setNeedGeneralSecAccount(true)
                .build();
        TrdGetAccList.Request req = TrdGetAccList.Request.newBuilder().setC2S(c2s).build();
        int seqNo = trading_conn.getAccList(req);
        logger.debug("Send TrdGetAccList: {}\n", seqNo);
    }

    public void getFutAcctList() {
        TrdGetAccList.C2S c2s = TrdGetAccList.C2S.newBuilder().setUserID(this.futuId)
                .setTrdCategory(TrdCommon.TrdCategory.TrdCategory_Future_VALUE)
                .setNeedGeneralSecAccount(true)
                .build();
        TrdGetAccList.Request req = TrdGetAccList.Request.newBuilder().setC2S(c2s).build();
        int seqNo = trading_conn.getAccList(req);
        logger.debug("Send TrdGetAccList: {}\n", seqNo);
    }

    @Override
    public void onReply_GetAccList(FTAPI_Conn client, int nSerialNo, TrdGetAccList.Response rsp) {
        if (rsp.getRetType() != 0) {
            logger.error("TrdGetAccList failed: {}\n", rsp.getRetMsg());
        } else {
            for (TrdCommon.TrdAcc acc : rsp.getS2C().getAccListList()) {

                if (acc.getTrdEnv() == 0 || (acc.getTrdEnv() == 1 && acc.getAccType() == 1)) //remove stimulated env )
                    continue;
                acctBook.put(acc.getAccID(), acc);
                logger.debug("Set Account ID: {}\n", acc.getAccID());

            }

        }
    }


    @Override
    public void onReply_GetPositionList(FTAPI_Conn client, int nSerialNo, TrdGetPositionList.Response rsp) {
        if (rsp.getRetType() != 0) {
            logger.error("TrdGetPositionList failed: \n" + rsp.getRetMsg());
        } else {
            try {
                String json = JsonFormat.printer().print(rsp);
                logger.debug("Receive TrdGetPositionList: {}\n", json);
                for (TrdCommon.Position position : rsp.getS2C().getPositionListList()) {
                    positionBook.put(position.getCode(), position);
                }

            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
    }

    public void putOrder(TrdPlaceOrder.Request req) {
        //Set TrdPlaceOrder.Request
        logger.info("Received Ordered from ORM.");
        if (this.isLiveOrder)
            trading_conn.placeOrder(req);
        else
            logger.info("Order not placed as live order trigger is not enabled.");

    }

    public void unlockLiveOrder() {
        //TODO: add some secuirty check

        this.isLiveOrder = true;
    }

    @Override
    public void onReply_PlaceOrder(FTAPI_Conn client, int nSerialNo, TrdPlaceOrder.Response rsp) {
        if (rsp.getRetType() != 0) {
            logger.fatal("TrdPlaceOrder failed: %s\n", rsp.getRetMsg());
        } else {
            try {
                String json = JsonFormat.printer().print(rsp);
                System.out.printf("Receive TrdPlaceOrder: %s\n", json);
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        logger.info("Attempting to connect to Futu Trading server...");
        try {
            trading_conn.initConnect(this.endpoint.toString(), this.port, false);
            logger.info("Successfully connected to Futu Trading server.");
            this.isActive = true;
        } catch (Exception e) {
            logger.error("Exception during Futu Trading connection: {}", e.getMessage(), e);
        }
    }

    public HashMap<String, TrdCommon.Position> getPositionBook() {
        return this.positionBook;
    }

    public boolean isReady() {
        return trading_conn.getConnStatus() == ConnStatus.READY;
    }

    public void loadPosition() {
        if (this.acctBook.keySet() == null)
            return;
        for (long keys : this.acctBook.keySet()) {
            getFunds(keys);
            getPosition(keys);
        }

    }


}
