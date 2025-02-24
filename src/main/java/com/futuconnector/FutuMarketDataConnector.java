package com.futuconnector;

import com.futu.openapi.FTAPI_Conn;
import com.futu.openapi.FTAPI_Conn_Qot;
import com.futu.openapi.FTSPI_Conn;
import com.futu.openapi.FTSPI_Qot;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;

public class FutuMarketDataConnector implements FTSPI_Qot, FTSPI_Conn {
    private static final Logger logger = LogManager.getLogger("FutuMarketDataConnector");
    FTAPI_Conn_Qot quoter = new FTAPI_Conn_Qot();
    URI endpoint ;
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

    @Override
    public void onDisconnect(FTAPI_Conn client, long errCode) {
        logger.error("FutuMarketDataConnector onDisConnect: %d\n", errCode);
    }


    public void connect() {
        logger.info("Attempting to connect to Futu Market Data server...");
        try {
            quoter.initConnect(this.endpoint.toString(),this.port,false);
            //TODO : Check if encryption is required
            logger.info("Successfully connected to Futu Market Data server.");
        } catch (Exception e) {
            logger.error("Exception during Futu Market Data connection: {}", e.getMessage(), e);
        }
    }

}
