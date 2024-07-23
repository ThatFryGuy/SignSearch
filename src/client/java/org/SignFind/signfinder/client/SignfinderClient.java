package org.SignFind.signfinder.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;


public class SignfinderClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Register commands only for the client
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal("findsigns")
                    .executes(context -> SignfinderCommand.execute(context)));
        });
    }
}

