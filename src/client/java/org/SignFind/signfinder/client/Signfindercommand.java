package org.SignFind.signfinder.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
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
import net.minecraft.world.level.block.entity.SignText;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

class SignfinderCommand {

    public static void registerClientCommands(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("findsigns")
                .then(ClientCommandManager.argument("radius", IntegerArgumentType.integer())
                        .then(ClientCommandManager.argument("location", StringArgumentType.string())
                                .then(ClientCommandManager.argument("include", StringArgumentType.string())
                                        .executes(context -> execute(context, IntegerArgumentType.getInteger(context, "radius"),
                                                StringArgumentType.getString(context, "location"),
                                                StringArgumentType.getString(context, "include"))))))
                .executes(context -> execute(context, 75, "default", ""))); // Default radius, location, and include term
    }

    public static int execute(@NotNull CommandContext<FabricClientCommandSource> context, int radius, String location, String include) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) {
            return 0; // Early exit if client or player is not available
        }

        ClientLevel world = client.level;
        BlockPos playerPos = client.player.blockPosition();

        File file = new File("sign_data.csv");

        int signCount = 0;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) { // Append to the file
            if (file.length() == 0) {
                writer.write("Location,X,Y,Z,Line 1,Line 2,Line 3,Line 4");
                writer.newLine();
            }

            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
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

                            boolean includeTermFound = false;
                            for (Component line : lines) {
                                if (line.getString().contains(include)) {
                                    includeTermFound = true;
                                    break;
                                }
                            }

                            if (!includeTermFound) {
                                continue;
                            }

                            writer.write(String.format("%s,%d,%d,%d,%s,%s,%s,%s",
                                    location,
                                    pos.getX(), pos.getY(), pos.getZ(),
                                    lines[0].getString().replace(",", ";"),
                                    lines[1].getString().replace(",", ";"),
                                    lines[2].getString().replace(",", ";"),
                                    lines[3].getString().replace(",", ";")));
                            writer.newLine();

                            signCount++;
                        }
                    }
                }
            }

            MutableComponent message;
            if (signCount > 0) {
                message = Component.literal(signCount + " signs found within a " + radius + "-block radius at location: " + location);
            } else {
                message = Component.literal("No signs found within a " + radius + "-block radius at location: " + location);
            }
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
