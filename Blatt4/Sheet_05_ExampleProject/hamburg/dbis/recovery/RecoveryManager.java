package hamburg.dbis.recovery;

import java.io.*;
import java.util.*;

public class RecoveryManager {

    static final private RecoveryManager _manager;

    static {
        try {
            _manager = new RecoveryManager();
        } catch (Throwable e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private RecoveryManager() {
        // Nothing needed here for now
    }

    public static RecoveryManager getInstance() {
        return _manager;
    }

    public void startRecovery() {
        System.out.println("Starting recovery...");

        File logFile = new File("log.txt");
        if (!logFile.exists()) {
            System.out.println("No log file found. Nothing to recover.");
            return;
        }

        // === Step 1: Read log and collect entries ===
        List<LogEntry> logEntries = new ArrayList<>();
        Set<Integer> committedTransactions = new HashSet<>();

        try (BufferedReader br = new BufferedReader(new FileReader(logFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                int lsn = Integer.parseInt(parts[0]);
                int taid = Integer.parseInt(parts[1]);

                if (parts.length == 3 && "EOT".equals(parts[2])) {
                    committedTransactions.add(taid);
                } else if (parts.length == 4) {
                    int pageid = Integer.parseInt(parts[2]);
                    String data = parts[3];
                    logEntries.add(new LogEntry(lsn, taid, pageid, data));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // === Step 2: Redo committed writes if page is outdated ===
        for (LogEntry entry : logEntries) {
            if (committedTransactions.contains(entry.taid)) {
                String filename = "Page_" + entry.pageid + ".txt";
                File pageFile = new File(filename);
                int pageLSN = -1;

                if (pageFile.exists()) {
                    try (BufferedReader br = new BufferedReader(new FileReader(pageFile))) {
                        String[] parts = br.readLine().split(",", 2);
                        pageLSN = Integer.parseInt(parts[0]);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (entry.lsn > pageLSN) {
                    try (BufferedWriter bw = new BufferedWriter(new FileWriter(pageFile))) {
                        bw.write(String.format("%04d,%s", entry.lsn, entry.data));
                        System.out.println("Redo: Page " + entry.pageid + " ← " + entry.data + " [LSN=" + entry.lsn + "]");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        System.out.println("Recovery complete.");
    }

    // === Helper class to represent log records ===
    private static class LogEntry {
        int lsn;
        int taid;
        int pageid;
        String data;

        LogEntry(int lsn, int taid, int pageid, String data) {
            this.lsn = lsn;
            this.taid = taid;
            this.pageid = pageid;
            this.data = data;
        }
    }
}
