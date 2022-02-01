package states.Migration;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import states.API.SMassiveAPI;
import states.Main.AncapStates;
import states.States.City.City;
import states.Database.Database;
import states.States.Nation.Nation;
import states.Player.AncapPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Migrator {

    private static Logger logger = Bukkit.getServer().getLogger();

    private static FileConfiguration oldDB = AncapStates.getInstance().getConfig();

    private Database statesDB = Database.STATES_DATABASE;

    public void startMigration() {
        LegacyPlayer[] legacyPlayers = getLegacyPlayers();
        logger.info("[MIGRATING TOOL] PlayerMigratingTask started");
        for (int i = 0; i<legacyPlayers.length; i++) {
            try {
                this.migratePlayer(legacyPlayers[i]);
            } catch (Exception e) {
                logger.info("[MIGRATING TOOL] Error occurred while migrating "+legacyPlayers[i].getName());
                e.printStackTrace();
            }
        }
        logger.info("[MIGRATING TOOL] PlayerMigratingTask done");
        LegacyCity[] legacyCities = getLegacyCities();
        logger.info("[MIGRATING TOOL] CityMigratingTask started");
        for (int i = 0; i<legacyCities.length; i++) {
            try {
                this.migrateCity(legacyCities[i]);
            } catch (Exception e) {
                logger.info("[MIGRATING TOOL] Error occurred while migrating "+legacyCities[i].getName());
                e.printStackTrace();
            }
        }
        logger.info("[MIGRATING TOOL] CityMigratingTask done");
        LegacyNation[] legacyNations = getLegacyNations();
        logger.info("[MIGRATING TOOL] NationMigratingTask started");
        for (int i = 0; i<legacyNations.length; i++) {
            try {
                this.migrateNation(legacyNations[i]);
            } catch (Exception e) {
                logger.info("[MIGRATING TOOL] Error occurred while migrating "+legacyCities[i].getName());
                e.printStackTrace();
            }
        }
        statesDB.save();
        logger.info("[MIGRATING TOOL] NationMigratingTask done");
    }

    private LegacyCity[] getLegacyCities() {
        String[] legacyCityNames = SMassiveAPI.toMassive(oldDB.getString("states.cityList"));
        LegacyCity[] cities = new LegacyCity[legacyCityNames.length];
        for (int i = 0; i<legacyCityNames.length; i++) {
            cities[i] = new LegacyCity(legacyCityNames[i]);
        }
        return cities;
    }

    private LegacyNation[] getLegacyNations() {
        String[] legacyNationsNames = getStringListFromOldDB("states.nation");
        LegacyNation[] nations = new LegacyNation[legacyNationsNames.length];
        for (int i = 0; i<legacyNationsNames.length; i++) {
            nations[i] = new LegacyNation(legacyNationsNames[i]);
        }
        return nations;
    }

    private LegacyPlayer[] getLegacyPlayers() {
        LegacyCity[] cities = this.getLegacyCities();
        ArrayList<LegacyPlayer> players = new ArrayList<>();
        for (int i = 0; i< cities.length; i++) {
            LegacyPlayer[] residents = cities[i].getResidents();
            players.addAll(List.of(residents));
        }
        return players.toArray(new LegacyPlayer[0]);
    }

    private void migratePlayer(LegacyPlayer legacyPlayer) {
        AncapPlayer player = legacyPlayer.getMigratedPlayer();
        statesDB.writeNoSave("states.player."+player.getID()+".name", legacyPlayer.getName());
        String cityID = null;
        LegacyCity legacyCity = legacyPlayer.getCity();
        if (legacyCity != null) {
            cityID = legacyCity.getMigratedCity().getID();
        }
        statesDB.writeNoSave("states.player."+player.getID()+".city", cityID);
        logger.info("[MIGRATING TOOL] Player "+player.getName()+" was migrated");
    }

    private void migrateCity(LegacyCity legacyCity) {
        City city = legacyCity.getMigratedCity();
        statesDB.writeNoSave("states.city."+city.getID()+".name", legacyCity.getName());
        statesDB.writeNoSave("states.city."+city.getID()+".board", legacyCity.getBoard());
        statesDB.writeNoSave("states.city."+city.getID()+".mayor", legacyCity.getLeader().getMigratedPlayer().getName());
        statesDB.writeNoSave("states.city."+city.getID()+".home", AncapStates.getInstance().convertLocation(legacyCity.getHome()));
        String residentsString = "";
        LegacyPlayer[] legacyResidents = legacyCity.getResidents();
        for (int i = 0; i<legacyResidents.length; i++) {
            AncapPlayer resident = legacyResidents[i].getMigratedPlayer();
            residentsString = residentsString+", "+resident.getID();
        }
        if (residentsString.equals("")) {
            residentsString = null;
        }
        statesDB.writeNoSave("states.city."+city.getID()+".residents", residentsString);
        String assistantsString = "";
        LegacyPlayer[] legacyAssistants = legacyCity.getAssistants();
        for (int i = 0; i<legacyAssistants.length; i++) {
            AncapPlayer assistant = legacyAssistants[i].getMigratedPlayer();
            assistantsString = assistantsString+", "+assistant.getID();
        }
        if (assistantsString.equals("")) {
            assistantsString = null;
        }
        statesDB.writeNoSave("states.city."+city.getID()+".assistants", assistantsString);
        LegacyNation legacyNation = legacyCity.getNation();
        String nationID = null;
        if (legacyNation != null) {
            nationID = legacyNation.getMigratedNation().getID();
        }
        statesDB.writeNoSave("states.city."+city.getID()+".nation", nationID);
        statesDB.writeNoSave("states.city."+city.getID()+".allowlevel", String.valueOf(legacyCity.getAllowLevel()));
        city.reclaimHomeHexagon();
        logger.info("[MIGRATING TOOL] City "+city.getName()+" was migrated");
    }

    private void migrateNation(LegacyNation legacyNation) {
        Nation nation = legacyNation.getMigratedNation();
        statesDB.writeNoSave("states.nation."+nation.getID()+".name", legacyNation.getName());
        statesDB.writeNoSave("states.nation."+nation.getID()+".capital", legacyNation.getCapital().getMigratedCity().getID());
        statesDB.writeNoSave("states.nation."+nation.getID()+".board", legacyNation.getBoard());
        statesDB.writeNoSave("states.nation."+nation.getID()+".color", "#"+legacyNation.getColor());
        String citiesString = "";
        LegacyCity[] legacyCities = legacyNation.getCities();
        for (int i = 0; i<legacyCities.length; i++) {
            City city = legacyCities[i].getMigratedCity();
            citiesString = citiesString+", "+city.getID();
        }
        if (citiesString.equals("")) {
            citiesString = null;
        }
        statesDB.writeNoSave("states.nation."+nation.getID()+".cities", citiesString);
        String ministersString = "";
        LegacyPlayer[] legacyMinisters = legacyNation.getMinisters();
        for (int i = 0; i<legacyMinisters.length; i++) {
            AncapPlayer minister = legacyMinisters[i].getMigratedPlayer();
            ministersString = ministersString+", "+minister.getID();
        }
        if (ministersString.equals("")) {
            ministersString = null;
        }
        statesDB.writeNoSave("states.nation."+nation.getID()+".ministers", ministersString);
        logger.info("[MIGRATING TOOL] Nation "+nation.getName()+" was migrated");
    }

    private String[] getStringListFromOldDB(String path) {
        ConfigurationSection configurationSection = oldDB.getConfigurationSection(path);
        if (configurationSection == null) {
            return new String[0];
        }
        return oldDB.getConfigurationSection(path).getKeys(false).toArray(new String[0]);
    }
}
