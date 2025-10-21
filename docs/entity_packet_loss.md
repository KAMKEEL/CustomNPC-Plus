# Entity spawn desync and third-party fixes

## CustomNPC-Plus status quo
CustomNPC-Plus serializes every NPC spawn/update compound through `ByteBufUtils.writeNBT`, which writes the compressed payload length as a signed 16-bit short before the bytes. When the blob exceeds 32,767 bytes the length wraps, and the client fails to reconstruct the NBT, causing the entity to disappear client-side.【F:src/main/java/kamkeel/npcs/util/ByteBufUtils.java†L53-L72】【F:src/main/java/noppes/npcs/entity/EntityNPCInterface.java†L1724-L1760】

Because the same helper is used for many other data packets, any oversized NPC (or another packet in the same stream) can desynchronize subsequent spawns and make otherwise "light" NPCs vanish until the chunk is reloaded.【F:src/main/java/kamkeel/npcs/network/packets/data/npc/UpdateNpcPacket.java†L40-L47】

## What EntityPacketLoseFix changes
The external `EntityPacketLoseFix` mod replaces Forge's default `FMLNetworkHandler.getEntitySpawningPacket` call in `EntityTrackerEntry` with its own proxy. The proxy hand-builds the `FMLMessage.EntitySpawnMessage`, serializes it directly via reflection, and drops the packet entirely if serialization throws, preventing Forge from dispatching malformed packets to other clients.【e2d533†L1-L35】【e535b7†L1-L43】

This mixin is effectively a workaround for entity packets that blow up during serialization. It avoids forwarding the corrupt packet but does not eliminate the short-length overflow at the source, so the underlying CustomNPC-Plus spawn/update encoding still needs to be fixed to guarantee reliable spawns.
