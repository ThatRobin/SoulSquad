package io.github.thatrobin.soul_squad.screens;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.thatrobin.soul_squad.SoulSquad;
import io.github.thatrobin.soul_squad.component.BodyHolderComponent;
import io.github.thatrobin.soul_squad.networking.HivemindPackets;
import io.github.thatrobin.soul_squad.powers.BodyManagementPower;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameModeSelectionScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;

import java.util.List;
import java.util.Optional;

public class BodySelectionScreen extends Screen {

    static final Identifier TEXTURE = SoulSquad.hivemind("textures/gui/body_switcher.png");
    private static final int UI_WIDTH = BodySelection.values().length * 31 - 5;
    private static final Text SELECT_NEXT_TEXT = Text.literal("[").append(Text.keybind("key.hivemind.cycle_bodies").formatted(Formatting.AQUA)).append(Text.literal("] ").append(Text.translatable("debug.gamemodes.select_next")));
    private final Optional<BodySelection> currentBody;
    private Optional<BodySelection> gameMode = Optional.empty();
    private int lastMouseX;
    private int lastMouseY;
    private boolean mouseUsedForSelection;
    private final List<ButtonWidget> gameModeButtons = Lists.newArrayList();

    public BodySelectionScreen() {
        super(NarratorManager.EMPTY);
        this.currentBody = BodySelection.of(MinecraftClient.getInstance().player);
    }

    @Override
    protected void init() {
        super.init();
        this.gameMode = this.currentBody.isPresent() ? this.currentBody : BodySelection.of(MinecraftClient.getInstance().player);
        for (int i = 0; i < BodySelection.VALUES.length; ++i) {
            BodySelection gameModeSelection = BodySelection.VALUES[i];
            if (MinecraftClient.getInstance().player != null) {
                BodyHolderComponent component = BodyHolderComponent.KEY.get(MinecraftClient.getInstance().player);
                gameModeSelection.player = component.getBody(gameModeSelection.bodyIndex);
            }
            this.gameModeButtons.add(new ButtonWidget(gameModeSelection, this.width / 2 - UI_WIDTH / 2 + i * 31, this.height / 2 - 31));
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (this.checkForClose()) {
            return;
        }
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        matrices.push();
        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, TEXTURE);
        int i = this.width / 2 - 62;
        int j = this.height / 2 - 31 - 27;
        GameModeSelectionScreen.drawTexture(matrices, i, j, 0.0f, 0.0f, 125, 75, 128, 128);
        matrices.pop();
        super.render(matrices, mouseX, mouseY, delta);
        this.gameMode.ifPresent(gameMode -> GameModeSelectionScreen.drawCenteredText(matrices, this.textRenderer, gameMode.getText(), this.width / 2, this.height / 2 - 31 - 20, -1));
        GameModeSelectionScreen.drawCenteredText(matrices, this.textRenderer, SELECT_NEXT_TEXT, this.width / 2, this.height / 2 + 5, 0xFFFFFF);
        if (!this.mouseUsedForSelection) {
            this.lastMouseX = mouseX;
            this.lastMouseY = mouseY;
            this.mouseUsedForSelection = true;
        }
        boolean bl = this.lastMouseX == mouseX && this.lastMouseY == mouseY;
        for (ButtonWidget buttonWidget : this.gameModeButtons) {
            buttonWidget.render(matrices, mouseX, mouseY, delta);
            this.gameMode.ifPresent(gameMode -> {
                buttonWidget.setSelected(gameMode == buttonWidget.gameMode);
                buttonWidget.active = buttonWidget.gameMode.player != null;
            });
            if (bl || !buttonWidget.isHovered()) continue;
            this.gameMode = Optional.of(buttonWidget.gameMode);
        }
    }

    private void apply() {
        BodySelectionScreen.apply(this.client, this.gameMode);
    }

    private static void apply(MinecraftClient client, Optional< BodySelection > gameMode) {
        BodySelection gameModeSelection = gameMode.get();
        if(gameModeSelection.player != null) {
            Optional<BodyManagementPower> powerOptional = PowerHolderComponent.getPowers(client.player, BodyManagementPower.class).stream().findFirst();
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            powerOptional.ifPresent((power) -> {
                buf.writeInt(gameModeSelection.bodyIndex);
                ClientPlayNetworking.send(HivemindPackets.SWAP_BODIES, buf);
            });
        }
    }

    private boolean checkForClose() {
        if (!InputUtil.isKeyPressed(this.client.getWindow().getHandle(), KeyBindingHelper.getBoundKeyOf(SoulSquad.OPEN_CYCLE_SCREEN).getCode())) {
            this.apply();
            this.client.setScreen(null);
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (SoulSquad.CYCLE_BODIES.matchesKey(keyCode, scanCode) && this.gameMode.isPresent()) {
            this.mouseUsedForSelection = false;
            this.gameMode = this.gameMode.get().next();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Environment(value= EnvType.CLIENT)
    public enum BodySelection {
        ORIGINAL(Text.translatable("body.hivemind.original"), new ItemStack(Items.RED_BED), 0),
        BODY_ONE(Text.translatable("body.hivemind.one"), new ItemStack(Items.IRON_PICKAXE), 1),
        BODY_TWO(Text.translatable("body.hivemind.two"), new ItemStack(Items.WHEAT), 2);
        private static final BodySelection[] VALUES;
        final Text text;
        int bodyIndex;
        public PlayerEntity player;
        ItemStack icon;

        private BodySelection(Text text, ItemStack icon, int bodyIndex) {
            this.bodyIndex = bodyIndex;
            this.text = text;
            this.icon = icon;

            if (MinecraftClient.getInstance().player != null) {
                BodyHolderComponent component = BodyHolderComponent.KEY.get(MinecraftClient.getInstance().player);
                this.player = component.getBody(bodyIndex);
            }
        }

        void renderIcon(ItemRenderer itemRenderer, int x, int y) {
            if(this.player != null) {
                this.icon = this.player.getMainHandStack().isEmpty() ? this.icon : this.player.getMainHandStack();
            }
            itemRenderer.renderInGuiWithOverrides(this.icon, x, y);
        }

        Text getText() {
            if(this.player != null) {
                ItemStack stack = this.player.inventory.armor.get(3);
                if (!stack.isEmpty() && stack.hasCustomName()) {
                    return stack.getName();
                }
            }
            return this.text;
        }

        Optional<BodySelection> next() {
            switch (this) {
                case ORIGINAL -> {
                    return Optional.of(BODY_ONE);
                }
                case BODY_ONE -> {
                    return Optional.of(BODY_TWO);
                }
                case BODY_TWO -> {
                    return Optional.of(ORIGINAL);
                }
            }
            return Optional.of(ORIGINAL);
        }

        static Optional<BodySelection> of(PlayerEntity entity) {
            if(entity != null) {
                if (entity.getDisplayName().getString().endsWith("-1")) {
                    return Optional.of(BODY_ONE);
                } else if (entity.getDisplayName().getString().endsWith("-2")) {
                    return Optional.of(BODY_TWO);
                } else {
                    return Optional.of(ORIGINAL);
                }
            }
            return Optional.of(ORIGINAL);
        }

        static {
            VALUES = BodySelection.values();
        }
    }

    @Environment(value=EnvType.CLIENT)
    public class ButtonWidget extends ClickableWidget {
        final BodySelection gameMode;
        private boolean selected;
        private final ItemStack barrier = new ItemStack(Blocks.BARRIER);

        public ButtonWidget(BodySelection gameMode, int x, int y) {
            super(x, y, 26, 26, gameMode.getText());
            this.gameMode = gameMode;
        }

        @Override
        public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            this.drawBackground(matrices);
            this.gameMode.renderIcon(BodySelectionScreen.this.itemRenderer, this.getX() + 5, this.getY() + 5);
            if (this.selected) {
                this.drawSelectionBox(matrices);
            }
            if(this.gameMode.player == null) {
                this.innerRenderInGui(itemRenderer, barrier, this.getX() + 5, this.getY() + 5, 0);
            }
        }

        @Override
        public void appendClickableNarrations(NarrationMessageBuilder builder) {
            this.appendDefaultNarrations(builder);
        }

        @Override
        public boolean isHovered() {
                return super.isHovered() || this.selected;
            }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        private void drawBackground(MatrixStack matrices) {
            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            RenderSystem.setShaderTexture(0, TEXTURE);
            matrices.push();
            matrices.translate(this.getX(), this.getY(), 0.0f);
            GameModeSelectionScreen.ButtonWidget.drawTexture(matrices, 0, 0, 0.0f, 75.0f, 26, 26, 128, 128);
            matrices.pop();
        }

        private void drawSelectionBox(MatrixStack matrices) {
            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            RenderSystem.setShaderTexture(0, TEXTURE);
            matrices.push();
            matrices.translate(this.getX(), this.getY(), 0.0f);
            GameModeSelectionScreen.ButtonWidget.drawTexture(matrices, 0, 0, 26.0f * (this.gameMode.bodyIndex  + 1), 75.0f, 26, 26, 128, 128);
            matrices.pop();
        }

        private void innerRenderInGui(ItemRenderer itemRenderer, ItemStack itemStack, int x, int y, int seed) {
            if (itemStack.isEmpty()) {
                return;
            }
            BakedModel bakedModel = itemRenderer.getModel(itemStack, null, MinecraftClient.getInstance().player, seed);
            itemRenderer.zOffset = itemRenderer.zOffset + 100f;
            try {
                itemRenderer.renderGuiItemModel(itemStack, x, y, bakedModel);
            }
            catch (Throwable throwable) {
                CrashReport crashReport = CrashReport.create(throwable, "Rendering item");
                CrashReportSection crashReportSection = crashReport.addElement("Item being rendered");
                crashReportSection.add("Item Type", () -> String.valueOf(itemStack.getItem()));
                crashReportSection.add("Item Damage", () -> String.valueOf(itemStack.getDamage()));
                crashReportSection.add("Item NBT", () -> String.valueOf(itemStack.getNbt()));
                crashReportSection.add("Item Foil", () -> String.valueOf(itemStack.hasGlint()));
                throw new CrashException(crashReport);
            }
            itemRenderer.zOffset = itemRenderer.zOffset - 100f;
        }
    }
}