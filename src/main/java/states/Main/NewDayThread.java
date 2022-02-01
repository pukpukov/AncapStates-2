package states.Main;

import org.bukkit.Bukkit;
import states.Message.Message;
import states.Message.StateMessage;

import java.util.logging.Logger;

public class NewDayThread extends Thread {

    @Override
    public void run() {
        long time0 = System.currentTimeMillis();
        Logger log = Bukkit.getLogger();
        Message message = StateMessage.NEW_DAY;
        AncapStates.sendMessage(message);
        log.info("Transfering money...");
        AncapStates.collectTaxes();
        log.info("Grabbing taxes...");
        AncapStates.grabTaxes();
        long time1 = System.currentTimeMillis();
        long estimatedTime = time1-time0;
        log.info("New day started. Estimated time: "+estimatedTime);
    }
}