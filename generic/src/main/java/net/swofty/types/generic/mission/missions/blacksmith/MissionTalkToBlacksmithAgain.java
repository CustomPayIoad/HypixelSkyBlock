package net.swofty.types.generic.mission.missions.blacksmith;

import net.minestom.server.event.Event;
import net.swofty.types.generic.mission.MissionData;
import net.swofty.types.generic.mission.SkyBlockMission;
import net.swofty.types.generic.region.RegionType;
import net.swofty.types.generic.user.SkyBlockPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MissionTalkToBlacksmithAgain extends SkyBlockMission {
    @Override
    public String getID() {
        return "talk_to_blacksmith_again";
    }

    @Override
    public boolean hasNoEvent() {
        return true;
    }

    @Override
    public String getName() {
        return "Talk to the Blacksmith";
    }

    @Override
    public Map<String, Object> onStart(SkyBlockPlayer player, MissionData.ActiveMission mission) {
        mission.getNewObjectiveText().forEach(player::sendMessage);
        return new HashMap<>();
    }

    @Override
    public void onEnd(SkyBlockPlayer player, Map<String, Object> customData, MissionData.ActiveMission mission) {

    }

    @Override
    public Set<RegionType> getValidRegions() {
        return Set.of(RegionType.BLACKSMITH, RegionType.COAL_MINE);
    }

    @Override
    public Class<? extends Event> getEvent() {
        return null;
    }

    @Override
    public void run(Event tempEvent) {}
}
