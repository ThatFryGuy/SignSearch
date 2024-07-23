package org.SignFind.signfinder.client;

import net.fabricmc.api.ClientModInitializer;
import org.SignFind.signfinder.Signfindercommand;

public class SignfinderClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Register client-specific commands
        Signfindercommand.registerCommands();
    }
}
