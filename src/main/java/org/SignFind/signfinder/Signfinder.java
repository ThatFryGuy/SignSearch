package org.SignFind.signfinder;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
public class Signfinder implements ModInitializer {

    @Override
    public void onInitialize() {
        // Register the command
        CommandRegistrationCallback.EVENT.register(new Signfindercommand());
    }
}
