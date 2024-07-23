package org.SignFind.signfinder.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

public class SignfinderClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Register client-side commands
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> SignfinderCommand.registerClientCommands(dispatcher));
    }
}
