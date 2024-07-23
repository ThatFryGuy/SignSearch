package org.SignFind.signfinder;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandBuildContext;
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
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Signfindercommand implements CommandRegistrationCallback {

    @Override
    public void register(@NotNull CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment) {
        dispatcher.register(Commands.literal("findsigns")
                .executes(this::execute));
    }

    private int execute(@NotNull CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel world = source.getLevel();
        BlockPos playerPos = player.blockPosition();

        boolean found = false;
        Workbook workbook = null;
        Sheet sheet = null;
        File file = new File("sign_data.xlsx");

        try {
            if (file.exists()) {
                FileInputStream fileInputStream = new FileInputStream(file);
                workbook = new XSSFWorkbook(fileInputStream);
                sheet = workbook.getSheetAt(0);
                fileInputStream.close();
            } else {
                workbook = new XSSFWorkbook();
                sheet = workbook.createSheet("Sign Data");
            }

            int rowIndex = sheet.getLastRowNum() + 1;

            for (int x = -75; x <= 75; x++) {
                for (int y = -75; y <= 75; y++) {
                    for (int z = -75; z <= 75; z++) {
                        BlockPos pos = playerPos.offset(x, y, z);
                        BlockState state = world.getBlockState(pos);

                        // Skip processing non-sign blocks
                        if (!isSign(state)) {
                            continue;
                        }

                        BlockEntity blockEntity = world.getBlockEntity(pos);
                        if (blockEntity instanceof SignBlockEntity signBlockEntity) {
                            SignText signText = signBlockEntity.getText(true); // Changed to true

                            StringBuilder signTextString = new StringBuilder();
                            boolean hasText = false;

                            Row row = sheet.createRow(rowIndex++);
                            row.createCell(0).setCellValue(pos.getX());
                            row.createCell(1).setCellValue(pos.getY());
                            row.createCell(2).setCellValue(pos.getZ());

                            for (int i = 0; i < 4; i++) {
                                Component line = signText.getMessage(i, true); // Changed to true
                                String lineText = line.getString();

                                if (!lineText.isEmpty()) {
                                    row.createCell(3 + i).setCellValue(lineText); // Write each line to a new column
                                    signTextString.append(lineText).append("\n");
                                    hasText = true;
                                }
                            }

                            if (hasText) {
                                MutableComponent message = Component.literal("Sign found at: " + pos + " with text:\n" + signTextString.toString().trim());
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
            } else {
                // Save the Excel file
                try (FileOutputStream fileOut = new FileOutputStream(file)) {
                    workbook.write(fileOut);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return 1;
    }

    private boolean isSign(@NotNull BlockState state) {
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
