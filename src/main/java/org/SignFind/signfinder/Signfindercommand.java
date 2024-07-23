package org.SignFind.signfinder;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Signfindercommand {

    public static void registerCommands() {
        // Register the command using Fabric API
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(Commands.literal("findsigns")
                .executes(Signfindercommand::execute)));
    }

    private static int execute(@NotNull CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel world = source.getLevel();
        BlockPos playerPos = player.blockPosition();

        boolean found = false;
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
                            SignText signText = signBlockEntity.getText(true);

                            StringBuilder line1 = new StringBuilder();
                            StringBuilder line2 = new StringBuilder();
                            StringBuilder line3 = new StringBuilder();
                            StringBuilder line4 = new StringBuilder();

                            for (int i = 0; i < 4; i++) {
                                Component line = signText.getMessage(i, true);
                                String lineText = line.getString();

                                switch (i) {
                                    case 0 -> line1.append(lineText);
                                    case 1 -> line2.append(lineText);
                                    case 2 -> line3.append(lineText);
                                    case 3 -> line4.append(lineText);
                                }
                            }

                            writer.write(String.format("%d,%d,%d,%s,%s,%s,%s",
                                    pos.getX(), pos.getY(), pos.getZ(),
                                    line1.toString().replace(",", ";"),
                                    line2.toString().replace(",", ";"),
                                    line3.toString().replace(",", ";"),
                                    line4.toString().replace(",", ";")));
                            writer.newLine();

                            if (!line1.toString().isEmpty() || !line2.toString().isEmpty() ||
                                    !line3.toString().isEmpty() || !line4.toString().isEmpty()) {
                                MutableComponent message = Component.literal("Sign found at: " + pos + " with text:\n" +
                                        line1 + "\n" + line2 + "\n" + line3 + "\n" + line4);
                                source.sendSuccess(() -> message, false);
                                found = true;
                            }
                        }
                    }
                }
            }

            if (!found) {
                MutableComponent message = Component.literal("No signs found within a 75-block radius.");
                source.sendSuccess(() -> message, false);
            }

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
