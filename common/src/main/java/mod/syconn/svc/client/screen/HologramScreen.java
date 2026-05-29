package mod.syconn.svc.client.screen;

import mod.syconn.svc.blockentity.HoloProjectorBlockEntity;
import mod.syconn.svc.client.screen.components.CallMenuWidget;
import mod.syconn.svc.client.screen.components.ErrorWidget;
import mod.syconn.svc.client.screen.components.buttons.CallButton;
import mod.syconn.svc.client.screen.components.buttons.ExpandedButton;
import mod.syconn.svc.client.screen.components.buttons.RefreshButton;
import mod.syconn.svc.core.ModBlockEntities;
import mod.syconn.svc.network.Network;
import mod.syconn.svc.network.packets.server.HoloCallPacket;
import mod.syconn.svc.server.savedData.extra.CallData;
import mod.syconn.svc.utils.Constants;
import mod.syconn.svc.utils.generic.ListUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class HologramScreen extends Screen {

    private static final ResourceLocation HOLOGRAM_SCREEN = Constants.withId("textures/gui/hologram_screen.png");
    private final @Nullable BlockPos holoPos;
    private final @Nullable ItemStack stack;
    private final boolean secure = true; // TODO This should be a toggle on the screen
    private boolean singleplayer;
    private Page page = Page.CREATE_CALL;
    private String lastSearch = "";
    private CallMenuWidget callData;
    private EditBox searchBox;
    private CallButton callButton;
    private ErrorWidget errorWidget;
    private Component pageTitle;

    public HologramScreen(@Nullable BlockPos pos, @Nullable ItemStack stack) {
        super(Component.literal("Hologram Projector Screen"));
        this.holoPos = pos;
        this.stack = stack;
    }

    private int marginX() {
        return (this.width - 238) / 2;
    }

    @Override
    public void tick() {
        super.tick();
        this.searchBox.tick();
    }

    @Override
    protected void init() { // TODO SOUND PLAYS BEFORE PROJECTOR ENDS ALSO LOWKEY ANOYINGLY LOUD
        this.singleplayer = Minecraft.getInstance().isSingleplayer();
        var leftPos = (this.width - 236) / 2;
        var buttonSize = 231 / 3 - 9;
        var newMargin = this.marginX() + 3;
        var string = this.searchBox != null ? this.searchBox.getValue() : "";

        this.errorWidget = this.addRenderableWidget(new ErrorWidget(leftPos + 236 / 2, 20));
        this.callData = new CallMenuWidget(this, leftPos + 10, 92, this.page, this::addRenderableWidget);
        if (this.stack == null) this.callButton = this.addRenderableWidget(new CallButton(newMargin + 209, 74, 0.80f, CallButton.Type.START, "Start Call", this::createCall));

        this.addRenderableWidget(new ExpandedButton(leftPos + 10, 51, buttonSize, 20, Component.literal("Create Call"), button -> this.showPage(Page.CREATE_CALL)));
        this.addRenderableWidget(new ExpandedButton(leftPos + 86, 51, buttonSize, 20, Component.literal("Display"), button -> this.showPage(Page.DISPLAY)));
        this.addRenderableWidget(new ExpandedButton(leftPos + 162, 51, buttonSize, 20, Component.literal("Join Call"), button -> this.showPage(Page.JOIN_CALL)));
        this.addRenderableWidget(new RefreshButton(newMargin + 11, 90, this.callData::refresh));

        this.pageTitle = Component.literal("Create Call");
        this.searchBox = new EditBox(this.font, this.marginX() + 29, 75, 178, 13, Component.literal(string));
        this.searchBox.setMaxLength(26);
        this.searchBox.setHint(Component.literal("Search"));
        this.searchBox.setVisible(true);
        this.searchBox.setTextColor(16777215);
        this.searchBox.setValue(string);
        this.searchBox.setResponder(this::checkSearchStringUpdate);
        this.addWidget(this.searchBox);
        this.showPage(this.page);

        if (this.singleplayer) showPage(Page.DISPLAY);
    }

    private void showPage(Page page) {
        this.page = page;
        this.callData.setPage(this.page);
        switch (page) {
            case CREATE_CALL:
                if (this.stack == null) this.callButton.visible = true;
                this.pageTitle = Component.literal("Start Call");
                break;
            case DISPLAY:
                if (this.stack == null) this.callButton.visible = false;
                this.pageTitle = Component.literal("Display Mode");
                break;
            case JOIN_CALL:
                if (this.stack == null) this.callButton.visible = false;
                this.pageTitle = Component.literal("Join Call");
                break;
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics) {
        super.renderBackground(guiGraphics);

        var m = this.marginX() + 3;
        guiGraphics.blitNineSliced(HOLOGRAM_SCREEN, m, 64, 236, 143, 8, 236, 34, 1, 1);
        guiGraphics.blit(HOLOGRAM_SCREEN, m, 35, 0, 78, 236, 33);
        guiGraphics.blit(HOLOGRAM_SCREEN, m + 11, 76, 244, 2, 12, 12);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.minecraft == null) return;

        var leftPos = (this.width - 236) / 2;
        this.renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.minecraft.font, this.pageTitle, leftPos + 119, 40, -1);
        this.searchBox.render(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return this.callData.mouseScrolled(mouseX, mouseY, delta) || super.mouseScrolled(mouseX, mouseY, delta);
    }

    public void hologramData(List<CallData.Call> playerCalls) {
        this.callData.handleNetworkPacket(playerCalls);
    }

    public @Nullable ItemStack getStack() {
        return stack;
    }

    private void checkSearchStringUpdate(String newText) {
        newText = newText.toLowerCase(Locale.ROOT);
        if (!newText.equals(this.lastSearch)) {
            this.lastSearch = newText;
            this.showPage(this.page);
            this.callData.search(newText);
        }
    }

    private void createCall(Button button) {
        if (!this.callData.getCallMembers().isEmpty()) this.createCall(ListUtil.append(getCaller(), this.callData.getCallMembers()));
        else this.errorWidget.displayError("ERROR: You must add at least one player", 100);
    }

    public void createCall(List<CallData.Callee> callers) {
        var caller = this.getCaller();
        if (caller != null) {
            Network.CHANNEL.sendToServer(new HoloCallPacket(HoloCallPacket.Type.CREATE, UUID.randomUUID(), secure, callers));
            Minecraft.getInstance().setScreen(null);
        }
    }

    public void joinCall(UUID callId) {
        var caller = this.getCaller();
        if (caller != null) {
            Network.CHANNEL.sendToServer(new HoloCallPacket(HoloCallPacket.Type.CONNECT, callId, secure, List.of(getCaller())));
            Minecraft.getInstance().setScreen(null);
        }
    }

    public void leaveCall(UUID callId) {
        var caller = this.getCaller();
        if (caller != null) Network.CHANNEL.sendToServer(new HoloCallPacket(HoloCallPacket.Type.LEAVE, callId, secure, List.of(caller)));
    }

    public @Nullable CallData.Callee getCaller() {
//        var uuid = this.stack == null ? null : HologramData.HologramTag.getOrCreate(this.stack).itemId; TODO SOME SORT OF HANDHELD BS
        if (this.minecraft == null || this.minecraft.player == null || this.minecraft.level == null || this.minecraft.level.getBlockEntity(holoPos, ModBlockEntities.HOLO_PROJECTOR.get()).isEmpty()) return null;
        return new CallData.Callee(this.minecraft.player.getUUID(), true, CallData.ReceiverType.BLOCK, this.minecraft.level.getBlockEntity(holoPos, ModBlockEntities.HOLO_PROJECTOR.get()).get().receiverUUID);
    }

    @Environment(EnvType.CLIENT)
    public enum Page {
        CREATE_CALL,
        DISPLAY,
        JOIN_CALL
    }
}
