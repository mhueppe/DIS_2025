package hamburg.dbis.client;

import hamburg.dbis.utils.DataLoader;
import hamburg.dbis.utils.RandomHashSet;
import java.util.Random;

public class ClientManager {

    Random rnd = new Random();
    static final private ClientManager _manager;
    static {
        try {
            _manager = new ClientManager();
        } catch (Throwable e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    private ClientManager() {}
    static public ClientManager getInstance() { return _manager; }
    public int nextInt(int a, int b) { return a + rnd.nextInt(b - a); }

    public void startClients() {
        int totalClients = 3;
        int pagesPerClient = 20;
        int writesPerTx = 6; 

        RandomHashSet<String> exampleData = DataLoader.loadExampleData();
        int clientid = 1;

        for (int c = 0; c < totalClients; c++) {
            int minPage = 10 + c * pagesPerClient;
            int maxPage = minPage + pagesPerClient; 

            Schedule schedule = Schedule.createSchedule();
            for (int w = 0; w < writesPerTx; w++) {
                int page = nextInt(minPage, maxPage);
                String data = exampleData.getRandomElement();
                schedule.addOperation(page, data);
            }

            Client client = new Client(clientid++, schedule, 100, 2000);
            client.toggleClientDebugMessages(); 
            client.start();
        }
    }
}