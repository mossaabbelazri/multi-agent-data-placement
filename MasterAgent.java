import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import java.util.*;
import java.io.*;
import java.net.InetAddress; // Pour le Ping

public class MasterAgent extends Agent {
    private HashMap<String, Double> scores = new HashMap<>();
    private HashMap<String, String[]> allData = new HashMap<>(); 
    private final int NB_WORKERS = 3;
    private final String CSV_FILE = "cluster_data.csv";
    private final int BLOC_SIZE = 128; // 1 bloc = 128 Mo

    // IP ou Noms d'hôtes VirtualBox (à vérifier dans votre fichier /etc/hosts)
    private Map<String, String> hostMapping = new HashMap<String, String>() {{
        put("W1", "Worker-1");
        put("W2", "Worker-2");
        put("W3", "Worker-3");
    }};

    protected void setup() {
        System.out.println("MasterAgent : Initialisation du monitoring intelligent (CPU/RAM/Disk/Latence)...");
        initialiserCSV();

        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    try {
                        String agentName = msg.getSender().getLocalName();
                        String hostName = hostMapping.getOrDefault(agentName, agentName);
                        
                        // --- MESURE DE LA LATENCE (PING) ---
                        long startPing = System.currentTimeMillis();
                        InetAddress.getByName(hostName).isReachable(1000); 
                        long latency = System.currentTimeMillis() - startPing;
                        if (latency <= 0) latency = 1; // Sécurité division par zéro

                        // --- DECODAGE DES DONNEES ---
                        String[] p = msg.getContent().split(",");
                        double ram = Double.parseDouble(p[0]);
                        double disk = Double.parseDouble(p[2]);
                        double tExec = Double.parseDouble(p[4]);
                        double wt = Double.parseDouble(p[5]);

                        // --- CALCULS ALGORITHMIQUES ---
                        if (tExec <= 0) tExec = 0.001; 
                        double ndt = tExec / wt;
                        
                        // Facteur de santé incluant la Latence
                        // Pondération : RAM(30%) + Disk(40%) + Latence(30%)
                        double f_sante = (0.3 * (ram/1536.0)) + (0.4 * (disk/25000.0)) + (0.3 * (1.0 / latency));
                        
                        // Sécurité : Si disque < 128 Mo, Capacité = 0
                        double Ci = (disk < 128) ? 0 : (1.0 / ndt) * f_sante * 1000;

                        scores.put(agentName, Ci);
                        
                        // Stockage : [RAM, Disk, Texec, Wt, NDT, Ci, Latency]
                        allData.put(agentName, new String[]{
                            String.valueOf((int)ram), String.valueOf((int)disk), 
                            String.format("%.3f", tExec), String.format("%.0f", wt), 
                            String.format("%.6f", ndt), String.format("%.2f", Ci),
                            String.valueOf(latency)
                        });

                        if (scores.size() == NB_WORKERS) { executerPlacementHadoop(); }
                    } catch (Exception e) { e.printStackTrace(); }
                } else { block(); }
            }
        });
    }

    private void executerPlacementHadoop() {
        int totalBlocks = 8; 
        double sommeCj = 0;
        for (double val : scores.values()) sommeCj += val;

        long ts = System.currentTimeMillis() / 1000;
        System.out.println("\n===== CYCLE DE PLACEMENT : " + ts + " =====");
        
        if (sommeCj <= 0) {
            System.err.println("!!! ALERTE : SATURATION TOTALE OU RÉSEAU COUPÉ !!!");
        }

        int blocsDistribues = 0;
        List<Map.Entry<String, Double>> list = new ArrayList<>(scores.entrySet());
        list.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        for (int i = 0; i < list.size(); i++) {
            String agentName = list.get(i).getKey();
            String hostName = hostMapping.getOrDefault(agentName, agentName); 
            double Ci = list.get(i).getValue();
            String[] d = allData.get(agentName); 
            
            long nbBlocs = 0;
            if (sommeCj > 0) {
                nbBlocs = Math.round((Ci / sommeCj) * totalBlocks);
                if (i == list.size() - 1) nbBlocs = totalBlocks - blocsDistribues;
                blocsDistribues += nbBlocs;
            }

            int tailleMo = (int)nbBlocs * BLOC_SIZE;

            // Affichage Terminal
            System.out.println("-> " + agentName + " [" + hostName + "] | Latence:" + d[6] + "ms | NDT:" + d[4]);
            System.out.println("   RAM:" + d[0] + "Mo | Disk:" + d[1] + "Mo | Score Ci:" + d[5]);
            System.out.println("   DÉCISION : " + nbBlocs + " Blocs (" + tailleMo + " Mo)");

            if (tailleMo > 0) {
                String nomFichier = "block_" + ts + ".dat";
                String cmd = "ssh " + hostName + " 'fallocate -l " + tailleMo + "M ~/" + nomFichier + "'";
                try {
                    Process p = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", cmd});
                    if (p.waitFor() == 0) System.out.println("   [SSH] OK.");
                } catch (Exception e) { e.printStackTrace(); }
            }

            // Sauvegarde CSV : Timestamp, Node, RAM, Disk, Wt, Texec, NDT, Ci, BAi, Latency
            try (PrintWriter pw = new PrintWriter(new FileWriter(CSV_FILE, true))) {
                pw.printf("%d,%s,%s,%s,%s,%s,%s,%s,%d,%s\n", ts, agentName, d[0], d[1], d[3], d[2], d[4], d[5], nbBlocs, d[6]);
            } catch (IOException e) { e.printStackTrace(); }
        }
        
        System.out.println("BILAN : " + blocsDistribues + " / " + totalBlocks + " blocs alloués.");
        System.out.println("=====================================================\n");
        scores.clear(); allData.clear();
    }

    private void initialiserCSV() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(CSV_FILE, false))) {
            pw.println("Timestamp,Node,RAM,Disk,Wt,Texec,NDT,Ci,BAi,Latency");
        } catch (IOException e) { e.printStackTrace(); }
    }
}
