package hamburg.dbis.persistence;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class PersistenceManager {

    static final private PersistenceManager _manager;

    private final Hashtable<Integer, BufferEntry> buffer = new Hashtable<>();
    
    private final AtomicInteger nextLSN = new AtomicInteger(1);
    private final AtomicInteger nextTransactionID = new AtomicInteger(1000);
    
    // taid => pageids 
    private final HashMap<Integer, HashSet<Integer>> transactionPageMap = new HashMap<>();
    // taid => committed or not
    private final HashMap<Integer, String> transactionStatus  = new HashMap<>();    

    private final String LOG_FILE = "log.txt";
    private final String PAGE_PREFIX = "page_";
    private final String PAGE_SUFFIX = ".txt";
    private final File logFile = new File(LOG_FILE);
    private final String COMMITED = "commited";
    private final String ACTIVE = "active";
    int lastTransactionId; 
    static {
        try {
            _manager = new PersistenceManager();
        } catch (Throwable e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    // Helper buffer entry class
    private static class BufferEntry {
        int lsn;
        String data;
        int taid;

        BufferEntry(int lsn, String data, int taid) {
            this.lsn = lsn;
            this.data = data;
            this.taid = taid;
        }
    }

    private PersistenceManager() {
        if (logFile.exists()){
            try (BufferedReader br = new BufferedReader(new FileReader(logFile))){
                String line; 
                int maxLSN = 0; 
                int maxTAID = 0; 
                while ((line = br.readLine())!=null) {
                    String[] parts = line.split(",", 4); 
                    int lsn = Integer.parseInt(parts[0]);
                    int taid = Integer.parseInt(parts[1]);
                    maxLSN = Math.max(maxLSN, lsn);
                    maxTAID = Math.max(maxTAID, taid);
                }
                nextLSN.set(maxLSN+1);
                nextTransactionID.set(maxTAID+1);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static public PersistenceManager getInstance() {
        return _manager;
    }

    public synchronized int beginTransaction() {
        int taid = nextTransactionID.getAndIncrement();
        transactionStatus.put(taid, ACTIVE);
        return taid;
    }

    
    public synchronized void commit(int taid) {
        if (!transactionStatus.containsKey(taid)) {
            throw new IllegalArgumentException("Unknown transaction: " + taid);
        }

        transactionStatus.put(taid, COMMITED);

        int lsn = nextLSN.getAndIncrement();
        appendToLog(String.format("%04d,%04d,EOT", lsn, taid));
    }

    public void write(int taid, int pageid, String data) {
        if (!transactionStatus.containsKey(taid)){
            throw new IllegalArgumentException("Transaction "+ taid + " no started.");
        }
        int lsn = nextLSN.getAndIncrement();

        // add to log
        appendToLog(String.format("%04d,%04d,%02d,%s", lsn, taid, pageid, data));
        
        // Buffer write
        buffer.put(pageid, new BufferEntry(lsn, data, taid));

        if (buffer.size() > 5){
            flushCommittedPages();
        }
        System.out.println("Buffer size: " + buffer.size());
    
    }

     private void flushCommittedPages() {
        List<Integer> toRemove = new ArrayList<>();
        for (Map.Entry<Integer, BufferEntry> entry : buffer.entrySet()) {
            int pageid = entry.getKey();
            BufferEntry be = entry.getValue();
            if (COMMITED.equals(transactionStatus.get(be.taid))) {
                // Write page to disk
                String filename = PAGE_PREFIX + pageid + PAGE_SUFFIX;
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
                    bw.write(String.format("%04d,%s", be.lsn, be.data));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                toRemove.add(pageid);
            }
        }
        for (int pid : toRemove) {
            buffer.remove(pid);
        }
        System.out.println("Buffer size after removing: " + buffer.size());

    }


    private void appendToLog(String line) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(logFile, true))) {
            bw.write(line);
            bw.newLine();
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

}

