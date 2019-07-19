package com.ranjith.databasechange;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.*;
import com.pusher.rest.Pusher;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ReadLog {

    private static String PRODUCT_TABLE_NAME = "products";

    public static void main(String[] args) throws IOException {
        final Map<String, Long> tableMap = new HashMap<String, Long>();

        Pusher pusher = new Pusher("807015", "e51d12d4da9641713f8e", "78e48d1432fcdd0b99fa");
        pusher.setCluster("ap2");
        pusher.setEncrypted(true);

        BinaryLogClient client = new BinaryLogClient("localhost", 3306, "user", "user");

        client.registerEventListener(event -> {
            EventData data = event.getData();

            if(data instanceof TableMapEventData) {
                TableMapEventData tableData = (TableMapEventData)data;
                tableMap.put(tableData.getTable(), tableData.getTableId());
            } else if(data instanceof WriteRowsEventData) {
                WriteRowsEventData eventData = (WriteRowsEventData)data;
                if(eventData.getTableId() == tableMap.get(PRODUCT_TABLE_NAME)) {
                    for(Object[] product: eventData.getRows()) {
                        pusher.trigger(PRODUCT_TABLE_NAME, "insert", getProductMap(product));
                    System.out.println("insert");
                    }
                }
            } else if(data instanceof UpdateRowsEventData) {
                UpdateRowsEventData eventData = (UpdateRowsEventData)data;
                if(eventData.getTableId() == tableMap.get(PRODUCT_TABLE_NAME)) {
                    for(Map.Entry<Serializable[], Serializable[]> row : eventData.getRows()) {
                        pusher.trigger(PRODUCT_TABLE_NAME, "update", getProductMap(row.getValue()));
                    }
                }
            } else if(data instanceof DeleteRowsEventData) {
                DeleteRowsEventData eventData = (DeleteRowsEventData)data;
                if(eventData.getTableId() == tableMap.get(PRODUCT_TABLE_NAME)) {
                    for(Object[] product: eventData.getRows()) {
                        pusher.trigger(PRODUCT_TABLE_NAME, "delete", product[0]);
                    }
                }
            }
        });
        client.connect();
    }

    static Map<String, String> getProductMap(Object[] product) {
        Map<String, String> map = new HashMap<>();
        map.put("id", java.lang.String.valueOf(product[0]));
        map.put("name", java.lang.String.valueOf(product[1]));
        map.put("price", java.lang.String.valueOf(product[2]));

        return map;
    }
}