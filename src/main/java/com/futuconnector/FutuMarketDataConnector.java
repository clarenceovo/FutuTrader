package com.futuconnector;

import com.futu.openapi.*;
import com.futu.openapi.pb.*;
import com.futuconnector.dto.MarketData.MarketOrderBook;
import com.futuconnector.dto.MarketData.MarketOrderBookCollection;
import com.futuconnector.dto.MarketData.MarketTicker;
import com.google.protobuf.InvalidProtocolBufferException;
import com.transport.InfluxDBClientManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.net.URI;

import com.google.protobuf.util.JsonFormat;

public class FutuMarketDataConnector implements FTSPI_Qot, FTSPI_Conn {
    private static final int HB_TIMEOUT = 10000; // 5 seconds
    private static final Logger logger = LogManager.getLogger("FutuMarketDataConnector");
    FTAPI_Conn_Qot quoter = new FTAPI_Conn_Qot();
    MarketOrderBookCollection orderbooks = new MarketOrderBookCollection();
    private InfluxDBClientManager influxTickerClient;
    private InfluxDBClientManager influxMktDataClient;
    private static FutuMarketDataConnector instance;
    private long lastTimestamp;
    private boolean isActive;
    URI endpoint;
    short port;


    public static synchronized FutuMarketDataConnector getInstance(String url, short port, InfluxDBClientManager influxTickerClient, InfluxDBClientManager influxMktDataClient) {
        logger.info("Creating FutuMarketDataConnector...");
        if (instance == null) {
            instance = new FutuMarketDataConnector(url, port, influxTickerClient, influxMktDataClient);
        }
        return instance;
    }

    public FutuMarketDataConnector(String url) {
        quoter.setClientInfo("FutuMarketData", 1);
        quoter.setConnSpi(this);
        quoter.setQotSpi(this);
        setLastTimestamp();
        try {
            this.endpoint = new URI(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private FutuMarketDataConnector(String url, short port, InfluxDBClientManager influxTickerClient, InfluxDBClientManager influxMktDataClient) {
        quoter.setClientInfo("FutuMarketData", 1);
        quoter.setConnSpi(this);
        quoter.setQotSpi(this);
        this.influxTickerClient = influxTickerClient;
        setLastTimestamp();
        try {
            this.endpoint = new URI(url);
            this.port = port;
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public void getMarketSector() {

    }

    public boolean isHeartbeatValid() {
        //logger.debug("HB diff: {}", System.currentTimeMillis() - lastTimestamp);
        return System.currentTimeMillis() - lastTimestamp < HB_TIMEOUT;
    }

    private void setLastTimestamp() {
        this.lastTimestamp = System.currentTimeMillis();
    }

    public void subscribeHKMarket(String symbol) {
        subscribe(symbol, QotCommon.SubType.SubType_Basic, QotCommon.QotMarket.QotMarket_HK_Security);
    }

    public void subscribeHKMarket(String symbol, QotCommon.SubType subType) {
        logger.info("Subscribing to {} , {}", symbol, subType.toString());
        subscribe(symbol, subType, QotCommon.QotMarket.QotMarket_HK_Security);
    }

    public void subscribeUSMarket(String symbol) {
        subscribe(symbol, QotCommon.SubType.SubType_Basic, QotCommon.QotMarket.QotMarket_US_Security);
    }

    public void subscribeJPMarket(String symbol) {
        subscribe(symbol, QotCommon.SubType.SubType_Basic, QotCommon.QotMarket.QotMarket_JP_Security);
    }

    public void subscribe(String symbol, QotCommon.SubType subType, QotCommon.QotMarket market) {
        QotCommon.Security sec = QotCommon.Security.newBuilder()
                .setMarket(market.getNumber())
                .setCode(symbol)
                .build();
        QotSub.C2S c2s = QotSub.C2S.newBuilder()
                .addSecurityList(sec)
                .addSubTypeList(subType.getNumber())
                .setIsSubOrUnSub(true)
                .setIsRegOrUnRegPush(true)
                .build();
        QotSub.Request req = QotSub.Request.newBuilder().setC2S(c2s).build();
        int seqNo = quoter.sub(req);
        logger.info("[Quote Sub seq:{}]Subscribe Quote: {}", seqNo, symbol);
    }


    @Override
    public void onReply_Sub(FTAPI_Conn client, int nSerialNo, QotSub.Response rsp) {
        setLastTimestamp();
        if (rsp.getRetType() != Common.RetType.RetType_Succeed_VALUE)
            return;

        try {
            String json = JsonFormat.printer().print(rsp);
            logger.info("Receive QotSub: {}", json);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

    }

    public void getSubscriptionInfo() {
        QotGetSubInfo.C2S c2s = QotGetSubInfo.C2S.newBuilder()
                .build();
        QotGetSubInfo.Request req = QotGetSubInfo.Request.newBuilder().setC2S(c2s).build();
        int seqNo = quoter.getSubInfo(req);
        logger.debug("GetSubInfo: {}", seqNo);

    }

    @Override
    public void onPush_UpdateTicker(FTAPI_Conn client, QotUpdateTicker.Response rsp) {
        setLastTimestamp();
        if (rsp.getRetType() != 0) {
            logger.info("QotUpdateTicker failed: {}", rsp.getRetMsg());
        } else {
            try {
                //String json = JsonFormat.printer().print(rsp);
                //logger.info("Receive Ticker: {}", json);
                MarketTicker tick = MarketTicker.from_ticker(rsp.getS2C().getSecurity().getCode(),
                        (long) rsp.getS2C().getTickerList(0).getTimestamp(),
                        BigDecimal.valueOf(rsp.getS2C().getTickerList(0).getPrice()),
                        BigDecimal.valueOf(rsp.getS2C().getTickerList(0).getVolume()));
                logger.info("[{}] Price: {} | Volume: {}", tick.getSymbol(), tick.getPrice(), tick.getVolume());
                influxTickerClient.writeData(tick.toPoint());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onReply_GetStaticInfo(FTAPI_Conn client, int nSerialNo, QotGetStaticInfo.Response rsp) {
        setLastTimestamp();
        if (rsp.getRetType() != 0) {
            logger.info("QotGetStaticInfo failed: {}", rsp.getRetMsg());
        } else {
            try {
                String json = JsonFormat.printer().print(rsp);
                logger.info("Receive QotGetStaticInfo: {}", json);
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onDisconnect(FTAPI_Conn client, long errCode) {
        logger.error("FutuMarketDataConnector onDisConnect: {}", errCode);
        //kill the process
        System.exit(1);
    }

    @Override
    public void onReply_GetSubInfo(FTAPI_Conn client, int nSerialNo, QotGetSubInfo.Response rsp) {
        setLastTimestamp();
        if (rsp.getRetType() != 0) {
            logger.info("QotGetSubInfo failed: {}", rsp.getRetMsg());
        } else {
            try {
                for (QotCommon.SubInfo x : rsp.getS2C().getConnSubInfoList(0).getSubInfoListList()) {
                    logger.info("Subscribed topic: {}", x.getSecurityListList());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onPush_UpdateBasicQuote(FTAPI_Conn client, QotUpdateBasicQot.Response rsp) {
        setLastTimestamp();
        if (rsp.getRetType() != 0) {
            logger.info("QotUpdateBasicQuote failed: %s", rsp.getRetMsg());
        } else {
            try {
                String json = JsonFormat.printer().print(rsp);
                logger.info("Receive QotUpdateBasicQuote: {}", json);
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onPush_UpdateOrderBook(FTAPI_Conn client, QotUpdateOrderBook.Response rsp) {
        setLastTimestamp();
        if (rsp.getRetType() != 0) {
            logger.info("QotUpdateOrderBook failed: %s", rsp.getRetMsg());
        } else {
            try {

                orderbooks.addOrderBook(MarketOrderBook.fromFUTUOrderBook(rsp.getS2C().getSecurity().getCode(),
                        System.currentTimeMillis(),
                        rsp.getS2C().getOrderBookBidListList(),
                        rsp.getS2C().getOrderBookAskListList()));
                MarketOrderBook book = orderbooks.getOrderBook(rsp.getS2C().getSecurity().getCode());
                logger.info("[{}] Mid Price: {} | Spread : {} | BidDepth : {} | AskDepth : {}", book.symbol, book.getMidPrice(), book.getSpread(), book.getBidDepth(), book.getAskDepth());
                ;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onPush_UpdateKL(FTAPI_Conn client, QotUpdateKL.Response rsp) {
        setLastTimestamp();
        if (rsp.getRetType() != 0) {
            logger.info("QotUpdateKL failed: %s", rsp.getRetMsg());
        } else {
            try {
                String json = JsonFormat.printer().print(rsp);
                logger.info("Receive QotUpdateKL: %s", json);
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onReply_GetTicker(FTAPI_Conn client, int nSerialNo, QotGetTicker.Response rsp) {
        setLastTimestamp();
        if (rsp.getRetType() != 0) {
            logger.error("QotGetTicker failed: {}", rsp.getRetMsg());
        } else {
            try {
                String json = JsonFormat.printer().print(rsp);
                logger.info("Receive QotGetTicker: {}", json);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onPush_UpdateRT(FTAPI_Conn client, QotUpdateRT.Response rsp) {
        setLastTimestamp();
        if (rsp.getRetType() != 0) {
            logger.info("QotUpdateRT failed: %s", rsp.getRetMsg());
        } else {
            try {
                String json = JsonFormat.printer().print(rsp);
                logger.info("Receive QotUpdateRT: %s", json);
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onReply_GetOptionChain(FTAPI_Conn client, int nSerialNo, QotGetOptionChain.Response rsp) {
        setLastTimestamp();
        if (rsp.getRetType() != 0) {
            logger.info("QotGetOptionChain failed: %s", rsp.getRetMsg());
        } else {
            try {
                String json = JsonFormat.printer().print(rsp);
                logger.info("Receive QotGetOptionChain: %s", json);
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
    }


    public boolean isActive() {
        return isActive;
    }

    public boolean isReady() {
        return quoter.getConnStatus() == ConnStatus.READY;
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
