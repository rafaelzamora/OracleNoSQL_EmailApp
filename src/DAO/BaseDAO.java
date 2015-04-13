package DAO;

import java.util.Iterator;
import oracle.kv.FaultException;

import oracle.kv.KVStore;
import oracle.kv.KVStoreConfig;
import oracle.kv.KVStoreFactory;
import oracle.kv.table.MultiRowOptions;
import oracle.kv.table.Row;
import oracle.kv.table.Table;
import oracle.kv.table.TableAPI;

public abstract class BaseDAO {

    private static KVStore kvStore = null;
    private static TableAPI tableImpl = null;
    private static String hostPort = "192.168.56.101:5000";
    private static String storeName = "KVSTORE";

    public BaseDAO() {
        super();

    } // BaseDAO

    public static KVStore getKVStore() {
        if (kvStore == null) {
            try {
                kvStore = KVStoreFactory.getStore(new KVStoreConfig(storeName,
                        hostPort));

            } catch (IllegalArgumentException | FaultException e) {

                System.exit(1);

            }
        } // EOF if(kvstore==null)

        return kvStore;
    } // getKVStore

    public static String getHostPort() {
        return hostPort;
    }

    public static void setHostPort(String hostPort) {
        BaseDAO.hostPort = hostPort;
    }

    public static String getStoreName() {
        return storeName;
    }

    public static void setStoreName(String storeName) {
        BaseDAO.storeName = storeName;
    }

    public static Table getTable(String tablePath) {
        Table table = null;
        try {
            tableImpl = getTableAPI();
            table = tableImpl.getTable(tablePath);
        } catch (Exception e) {
            System.err.println("Failed to get table: " + tablePath);
            // e.printStackTrace();
        }
        return table;
    }

    public static Iterator<Row> getTableRows(Table table) {

        MultiRowOptions mro = new MultiRowOptions(null, null, null);
        return tableImpl.tableIterator(table.createPrimaryKey(), mro, null);
    }

    public static TableAPI getTableAPI() {
        if (tableImpl == null) {
            tableImpl = getKVStore().getTableAPI();
        }
        return tableImpl;
    }

    /**
     * Each DAO class need to define how to create the table
     */
    public abstract void createTable();

}
