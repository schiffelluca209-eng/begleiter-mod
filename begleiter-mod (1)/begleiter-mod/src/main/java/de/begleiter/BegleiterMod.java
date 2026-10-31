package de.begleiter;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class BegleiterMod implements ModInitializer {
    public static final String MOD_ID = "begleiter";
    public static EntityType<BegleiterEntity> BEGLEITER;

    @Override
    public void onInitialize() {
        ResourceKey<EntityType<?>> key = ResourceKey.create(
                Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, "begleiter"));
        BEGLEITER = Registry.register(BuiltInRegistries.ENTITY_TYPE, key,
                EntityType.Builder.of(BegleiterEntity::new, MobCategory.MISC)
                        .sized(0.6f, 1.8f)
                        .clientTrackingRange(10)
                        .build(key));
        FabricDefaultAttributeRegistry.register(BEGLEITER, BegleiterEntity.createAttributes());

        CommandRegistrationCallback.EVENT.register(
                (dispatcher, buildContext, selection) -> CompanionCommands.register(dispatcher));
        ServerTickEvents.END_SERVER_TICK.register(CompanionManager::tick);
        ServerPlayConnectionEvents.DISCONNECT.register(
                (handler, server) -> CompanionManager.remove(handler.getPlayer().getUUID()));
    }
}
