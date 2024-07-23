package org.SignFind.signfinder.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.SignText; // Import SignText
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

class SignfinderCommand {

    public static void registerClientCommands(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("findsigns")
                .executes(SignfinderCommand::execute));
    }

    public static int execute(@NotNull CommandContext<FabricClientCommandSource> context) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) {
            return 0; // Early exit if client or player is not available
        }

        ClientLevel world = client.level;
        BlockPos playerPos = client.player.blockPosition();

        File file = new File("sign_data.csv");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("X,Y,Z,Line 1,Line 2,Line 3,Line 4");
            writer.newLine();

            for (int x = -75; x <= 75; x++) {
                for (int y = -75; y <= 75; y++) {
                    for (int z = -75; z <= 75; z++) {
                        BlockPos pos = playerPos.offset(x, y, z);
                        BlockState state = world.getBlockState(pos);

                        if (!isSign(state)) {
                            continue;
                        }

                        BlockEntity blockEntity = world.getBlockEntity(pos);
                        if (blockEntity instanceof SignBlockEntity signBlockEntity) {
                            SignText signText = signBlockEntity.getText(true); // Use getText with true

                            // Retrieve the lines from SignText
                            Component[] lines = new Component[4];
                            for (int i = 0; i < 4; i++) {
                                lines[i] = signText.getMessage(i, false); // Adjust method parameters as needed
                            }

                            writer.write(String.format("%d,%d,%d,%s,%s,%s,%s",
                                    pos.getX(), pos.getY(), pos.getZ(),
                                    lines[0].getString().replace(",", ";"),
                                    lines[1].getString().replace(",", ";"),
                                    lines[2].getString().replace(",", ";"),
                                    lines[3].getString().replace(",", ";")));
                            writer.newLine();

                            if (!lines[0].getString().isEmpty() || !lines[1].getString().isEmpty() ||
                                    !lines[2].getString().isEmpty() || !lines[3].getString().isEmpty()) {
                                MutableComponent message = Component.literal("Sign found at: " + pos + " with text:\n" +
                                        lines[0].getString() + "\n" + lines[1].getString() + "\n" + lines[2].getString() + "\n" + lines[3].getString());
                                client.player.sendSystemMessage(message);
                            }
                        }
                    }
                }
            }

            MutableComponent message = Component.literal("No signs found within a 75-block radius.");
            client.player.sendSystemMessage(message);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return 1;
    }

    private static boolean isSign(@NotNull BlockState state) {
        return state.getBlock() == Blocks.OAK_SIGN ||
                state.getBlock() == Blocks.SPRUCE_SIGN ||
                state.getBlock() == Blocks.BIRCH_SIGN ||
                state.getBlock() == Blocks.JUNGLE_SIGN ||
                state.getBlock() == Blocks.ACACIA_SIGN ||
                state.getBlock() == Blocks.DARK_OAK_SIGN ||
                state.getBlock() == Blocks.OAK_WALL_SIGN ||
                state.getBlock() == Blocks.SPRUCE_WALL_SIGN ||
                state.getBlock() == Blocks.BIRCH_WALL_SIGN ||
                state.getBlock() == Blocks.JUNGLE_WALL_SIGN ||
                state.getBlock() == Blocks.ACACIA_WALL_SIGN ||
                state.getBlock() == Blocks.DARK_OAK_WALL_SIGN;
    }
}
