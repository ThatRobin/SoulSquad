package io.github.thatrobin.soul_squad.networking;

import io.github.thatrobin.soul_squad.SoulSquad;
import net.minecraft.util.Identifier;

public class HivemindPackets {
    public static final Identifier UPDATE_SLOT = SoulSquad.hivemind("update_slot");
    public static final Identifier SWAP_BODIES = SoulSquad.hivemind("swap_bodies");
    public static final Identifier UPDATE_UUIDS = SoulSquad.hivemind("update_uuids");
    public static final Identifier UPDATE_MAP = SoulSquad.hivemind("update_map");
    public static final Identifier UPDATE_MAP_2 = SoulSquad.hivemind("update_map_2");
}
