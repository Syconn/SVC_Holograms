package mod.syconn.svc.client.screen;

import dev.architectury.networking.NetworkManager;
import mod.syconn.svc.client.screen.components.CallMenuWidget;
import mod.syconn.svc.client.screen.components.ErrorWidget;
import mod.syconn.svc.client.screen.components.buttons.*;
import mod.syconn.svc.core.ModBlockEntities;
import mod.syconn.svc.network.packets.server.HoloCallPacket;
import mod.syconn.svc.network.packets.server.PacketCallType;
import mod.syconn.svc.server.savedData.extra.CallData;
import mod.syconn.svc.utils.Constants;
import mod.syconn.svc.utils.generic.ListUtil;
import mod.syconn.svc.utils.item.HologramComponent;
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
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class HologramScreen extends Screen {

    private static final ResourceLocation HOLOGRAM_SPRITE = Constants.withId("hologram_screen.png");
    private static final ResourceLocation HOLOGRAM_SCREEN = Constants.withId("textures/gui/hologram_screen.png");
    private final @Nullable BlockPos holoPos;
    private final @Nullable ItemStack stack;
    private boolean singleplayer;
    private Page page = Page.CREATE_CALL;
    private String lastSearch = "";
    private CallMenuWidget callData;
    private SearchButton searchButton;
    public EditBox searchBox;
    private LockButton lockButton;
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
//        this.searchBox.tick(); TODO MAYBE REMOVED?
    }

    @Override
    protected void init() {
        this.singleplayer = Minecraft.getInstance().isSingleplayer();
        var leftPos = (this.width - 236) / 2;
        var buttonSize = 231 / 3 - 9;
        var newMargin = this.marginX() + 3;
        var string = this.searchBox != null ? this.searchBox.getValue() : "";

        this.errorWidget = this.addRenderableWidget(new ErrorWidget(leftPos + 236 / 2, 20));
        this.callData = new CallMenuWidget(this, leftPos + 10, 92, this.page, this::addRenderableWidget);
        if (this.stack == null) this.callButton = this.addRenderableWidget(new CallButton(newMargin + 209, 74, 0.80f, CallButton.Type.START, "Start Call", this::createCall));
        if (this.stack == null) this.lockButton = this.addRenderableWidget(new LockButton(this.marginX() + 215, 90, LockButton.Type.LOCK));
        this.searchButton = this.addRenderableWidget(new SearchButton(newMargin + 212, 78, "Search For Player", (b) -> this.callData.searchForPlayer()));

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
        if (this.singleplayer) showPage(Page.DISPLAY);
        else this.showPage(this.page);
    }

    private void showPage(Page page) {
        if (this.singleplayer && page != Page.DISPLAY) {
            this.errorWidget.displayError("Not available in Singleplayer", 250);
            return;
        }

        this.page = page;
        this.callData.setPage(this.page);
        switch (page) {
            case CREATE_CALL:
                if (this.stack == null) this.callButton.visible = true;
                if (this.stack == null) this.lockButton.visible = true;
                this.searchButton.visible = false;
                this.pageTitle = Component.literal("Start Call");
                break;
            case DISPLAY:
                if (this.stack == null) this.callButton.visible = false;
                if (this.stack == null) this.lockButton.visible = false;
                this.searchButton.visible = true;
                this.pageTitle = Component.literal("Display Mode");
                break;
            case JOIN_CALL:
                if (this.stack == null) this.callButton.visible = false;
                if (this.stack == null) this.lockButton.visible = false;
                this.searchButton.visible = false;
                this.pageTitle = Component.literal("Join Call");
                break;
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) { // TODO SocialInteractionsScreen
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        var m = this.marginX() + 3;
        guiGraphics.blitSprite(HOLOGRAM_SPRITE, m, 64, 236, 143);
        guiGraphics.blit(HOLOGRAM_SCREEN, m, 35, 0, 78, 236, 33);
        guiGraphics.blit(HOLOGRAM_SCREEN, m + 11, 76, 244, 2, 12, 12);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.minecraft == null) return;

        var leftPos = (this.width - 236) / 2;
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.minecraft.font, this.pageTitle, leftPos + 119, 40, DyeColor.WHITE.getTextColor());
        this.searchBox.render(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return this.callData.mouseScrolled(mouseX, mouseY, scrollX, scrollY) || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    public void hologramData(List<CallData.Call> playerCalls) {
        this.callData.handleNetworkPacket(playerCalls);
    }

    public @Nullable ItemStack getStack() {
        return stack;
    }

    public @Nullable BlockPos getHoloPos() {
        return holoPos;
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
        if (!this.callData.getCallMembers().isEmpty()) {
            var caller = this.getCaller();
            if (caller != null) {
                NetworkManager.sendToServer(new HoloCallPacket(PacketCallType.CREATE, UUID.randomUUID(), this.lockButton.getType() == LockButton.Type.LOCK, false, new BlockPos(0, 0, 0), 
                        ListUtil.append(getCaller(), this.callData.getCallMembers())));
                Minecraft.getInstance().setScreen(null);
            }
        }
        else this.errorWidget.displayError("ERROR: You must add at least one player", 100);
    }

    public void joinCall(UUID callId) {
        var caller = this.getCaller();
        if (caller != null) {
            NetworkManager.sendToServer(new HoloCallPacket(PacketCallType.CONNECT, callId, false, false, new BlockPos(0, 0, 0), List.of(getCaller())));
            Minecraft.getInstance().setScreen(null);
        }
    }

    public void leaveCall(UUID callId) {
        var caller = this.getCaller();
        if (caller != null) NetworkManager.sendToServer(new HoloCallPacket(PacketCallType.LEAVE, callId, false, false, new BlockPos(0, 0, 0), List.of(caller)));
    }

    public @Nullable CallData.Callee getCaller() {
        var uuid = this.stack == null ? null : HologramComponent.getOrCreate(this.stack).receiverID();
        if (this.minecraft == null || this.minecraft.player == null || this.minecraft.level == null) return new CallData.Callee(UUID.randomUUID());
        if (uuid != null) return new CallData.Callee(this.minecraft.player.getUUID(), true, CallData.ReceiverType.ITEM, uuid);
        return new CallData.Callee(this.minecraft.player.getUUID(), true, CallData.ReceiverType.BLOCK, this.minecraft.level.getBlockEntity(holoPos, ModBlockEntities.HOLO_PROJECTOR.get()).get().getReceiverUUID());
    }

    @Environment(EnvType.CLIENT)
    public enum Page {
        CREATE_CALL,
        DISPLAY,
        JOIN_CALL
    }
}
