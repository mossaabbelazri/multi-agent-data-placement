import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.TickerBehaviour;
import java.io.*;

public class SlaveAgent extends Agent {
    protected void setup() {
        System.out.println("Agent [" + getLocalName() + "] : Monitoring et Action activés.");
        
        addBehaviour(new TickerBehaviour(this, 10000) {
            protected void onTick() {
                String metrics = getSystemMetrics();
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceiver(new AID("MasterAgent", AID.ISLOCALNAME));
                msg.setContent(metrics);
                send(msg);
            }
        });
    }

    private String getSystemMetrics() {
        try {
            String ram = execute("free -m | grep Mem: | awk '{print $7}'");
            String disk = execute("df -m /home/hadoop | tail -1 | awk '{print $4}'");
            String cpu = execute("nproc");
            String latency = execute("ping -c 1 Master | tail -1 | awk -F '/' '{print $5}'");

            // Benchmark lourd pour Texec
            double wt = 512.0; 
            long startTime = System.currentTimeMillis();
            double temp = 0;
            for (int i = 0; i < 500000000; i++) { temp = Math.sqrt(i); }
            long endTime = System.currentTimeMillis();
            
            double tExec = (endTime - startTime) / 1000.0;
            return ram + "," + cpu + "," + disk + "," + latency + "," + tExec + "," + wt;
        } catch (Exception e) { return "0,0,0,1,0.5,512"; }
    }

    private String execute(String cmd) throws Exception {
        Process p = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", cmd});
        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String res = in.readLine();
        return (res != null) ? res.trim() : "0";
    }
}
