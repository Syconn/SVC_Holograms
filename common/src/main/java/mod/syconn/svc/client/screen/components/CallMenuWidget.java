package mod.syconn.svc.client.screen.components;

import com.mojang.authlib.GameProfile;
import dev.architectury.networking.NetworkManager;
import mod.syconn.svc.blockentity.HoloProjectorBlockEntity;
import mod.syconn.svc.client.screen.HologramScreen;
import mod.syconn.svc.client.screen.components.buttons.CallButton;
import mod.syconn.svc.client.screen.components.buttons.CheckButton;
import mod.syconn.svc.client.screen.components.buttons.ToggleButton;
import mod.syconn.svc.network.packets.c2s.HoloCallPacket;
import mod.syconn.svc.network.packets.c2s.PacketCallType;
import mod.syconn.svc.network.packets.c2s.RenderHoloPlayerPacket;
import mod.syconn.svc.network.packets.c2s.RequestHologramPacket;
import mod.syconn.svc.server.savedData.extra.CallData;
import mod.syconn.svc.utils.generic.ColorUtil;
import mod.syconn.svc.utils.generic.GraphicsUtil;
import mod.syconn.svc.utils.generic.ResourceUtil;
import mod.syconn.svc.utils.interfaces.IWidgetComponent;
import mod.syconn.svc.utils.item.HologramComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class CallMenuWidget implements IWidgetComponent {

    private final List<MenuData> listedCreateCallPlayers = new ArrayList<>();
    private final List<MenuData> listedJoinCallPlayers = new ArrayList<>();
    private final List<MenuData> listedSearchedPlayer = new ArrayList<>();
    private final List<MenuData> shownCreateCallPlayers = new ArrayList<>();
    private final List<MenuData> shownJoinCallPlayers = new ArrayList<>();
    private final List<MenuData> shownSearchedPlayer = new ArrayList<>();
    private final ToggleButton[] toggleButtons = new ToggleButton[3];
    private final CallButton[] callButtonsHandheld = new CallButton[3];
    private final PlayerCountWidget[] playerCountWidgets = new PlayerCountWidget[3];
    private final CallButton[] callButtons = new CallButton[6];
    private final CheckButton[] checkButtons = new CheckButton[6];
    private final Minecraft minecraft = Minecraft.getInstance();
    private final int height = 32;
    private final HologramScreen screen;
    private final int x, y;
    private final ItemStack stack;
    private int scroll = 0;
    private ScrollerWidget scroller;
    private HologramScreen.Page page;
    private String lastSearch;

    public CallMenuWidget(HologramScreen screen, int x, int y, HologramScreen.Page page, Function<IWidgetComponent, IWidgetComponent> widgets) {
        this.x = x;
        this.y = y;
        this.page = page;
        this.screen = screen;
        this.stack = this.screen.getStack();

        this.refreshPlayerList();
        this.init(widgets);
        this.updateMenu(this.scroll);
        this.search(this.lastSearch);
    }

    private void init(Function<IWidgetComponent, IWidgetComponent> widgets) {
        widgets.apply(this);

        for (int i = 0; i < 3; i++) {
            final var v = i;
            var row = this.height * v;
            this.toggleButtons[v] = (ToggleButton) widgets.apply(new ToggleButton(this.x + 180, this.y + 21 + row, false, ToggleButton.Color.GREEN, b -> this.toggled(b, v)));
            this.callButtonsHandheld[v] = (CallButton) widgets.apply(new CallButton(this.x + 180, this.y + 17 + row, CallButton.Type.START, "Begin Call", b -> this.callHandheldPressed(v)));
            this.playerCountWidgets[v] = (PlayerCountWidget) widgets.apply(new PlayerCountWidget(this.x + 195, this.y + 28 + row));
            this.callButtons[v] = (CallButton) widgets.apply(new CallButton(this.x + 34, this.y + 26 + row, 0.75f, CallButton.Type.START, "Join Call", b -> callPressed((CallButton) b, v)));
            this.callButtons[v + 3] = (CallButton) widgets.apply(new CallButton(this.x + 100, this.y + 26 + row, 0.75f, CallButton.Type.END, "Decline Call", b -> callPressed((CallButton) b, v)));
            this.checkButtons[v] = (CheckButton) widgets.apply(new CheckButton(this.x + 170, this.y + 22 + row, CheckButton.Type.CROSS, "End Render", b -> checkButton((CheckButton) b, v)));
            this.checkButtons[v + 3] = (CheckButton) widgets.apply(new CheckButton(this.x + 190, this.y + 21 + row, CheckButton.Type.CHECK, "Render", b -> checkButton((CheckButton) b, v)));
        }

        this.scroller = (ScrollerWidget) widgets.apply(new ScrollerWidget(x + 207, y + 11, 91, this.listedCreateCallPlayers.size() - 3, w -> true, this::updateMenu));
    }

    private void toggled(ToggleButton button, int i) {
        var player = this.shownCreateCallPlayers.get(this.scroll + i);
        this.shownCreateCallPlayers.set(this.scroll + i, new MenuData(player.profile, player.callID, List.of(), !button.active(), player.locked));
    }

    private void callHandheldPressed(int i) {
        var caller = this.screen.getCaller();
        if (caller != null) {
            NetworkManager.sendToServer(new HoloCallPacket(PacketCallType.CREATE, UUID.randomUUID(), true, false, new BlockPos(0, 0, 0), List.of(caller, new CallData.Callee(this.shownCreateCallPlayers.get(this.scroll + i).profile.getId()))));
            Minecraft.getInstance().setScreen(null);
        }
    }

    public void searchForPlayer() {
        if (!Objects.equals(this.screen.searchBox.getValue(), "")) ResourceUtil.loadGameProfile(this.screen.searchBox.getValue(), this::refresh);
    }

    private void callPressed(CallButton button, int i) {
        var uuid = this.shownJoinCallPlayers.get(this.scroll + i).callID;
        if (button.getType() == CallButton.Type.END) this.screen.leaveCall(uuid);
        else this.screen.joinCall(uuid);

        refresh();
    }

    private void checkButton(CheckButton button, int i) {
        var name = this.shownSearchedPlayer.get(this.scroll + i).profile.getName();
        var caller = this.screen.getCaller();
        var pos = this.screen.getHoloPos() == null ? new BlockPos(0, 0, 0) : this.screen.getHoloPos();
        if (caller != null) NetworkManager.sendToServer(new HoloCallPacket(PacketCallType.LEAVE, UUID.randomUUID(), false, true, pos, List.of(caller)));
        NetworkManager.sendToServer(new RenderHoloPlayerPacket(pos, this.screen.getHoloPos() == null, button.getType() == CheckButton.Type.CHECK ? name : ""));
        Minecraft.getInstance().setScreen(null);
    }

    private void refreshPlayerList() {
        if (this.minecraft.player != null) {
            this.listedCreateCallPlayers.clear();
            this.listedSearchedPlayer.clear();

            if (this.page == HologramScreen.Page.CREATE_CALL) {
                var connection = this.minecraft.player.connection;
                connection.getOnlinePlayerIds().forEach(uuid -> {
                    var info = connection.getPlayerInfo(uuid);
                    if (info != null) this.listedCreateCallPlayers.add(MenuData.ofCreate(info.getProfile(), isPlayerMe(info.getProfile())));
                });
            } else if (this.page == HologramScreen.Page.JOIN_CALL) NetworkManager.sendToServer(new RequestHologramPacket());
            else {
                final var listedNames = new HashSet<>();
                ResourceUtil.getPlayerProfiles().forEach(profile -> {
                    if (listedNames.add(profile.getName())) this.listedSearchedPlayer.add(MenuData.ofCreate(profile, isPlayerMe(profile)));
                });
                var connection = this.minecraft.player.connection;
                connection.getOnlinePlayerIds().stream().map(connection::getPlayerInfo).filter(Objects::nonNull).forEach(info -> {
                    if (listedNames.add(info.getProfile().getName())) this.listedSearchedPlayer.add(MenuData.ofCreate(info.getProfile(), isPlayerMe(info.getProfile())));
                });
            }
        }
    }

    private void updateMenu(int scroll) {
        this.scroll = scroll;

        if (this.page == HologramScreen.Page.CREATE_CALL) this.scroller.updateSize(this.shownCreateCallPlayers.size() - 3);
        else if (this.page == HologramScreen.Page.JOIN_CALL) this.scroller.updateSize(this.shownJoinCallPlayers.size() - 3);
        else this.scroller.updateSize(this.listedSearchedPlayer.size() - 3);

        Arrays.stream(this.toggleButtons).forEach(b -> b.visible = false);
        Arrays.stream(this.callButtonsHandheld).forEach(b -> b.visible = false);
        Arrays.stream(this.playerCountWidgets).forEach(b -> b.visible = false);
        Arrays.stream(this.callButtons).forEach(b -> b.visible = false);
        Arrays.stream(this.checkButtons).forEach(b -> b.visible = false);

        if (this.page == HologramScreen.Page.CREATE_CALL) {
            for (int i = scroll; i < Math.min(scroll + 3, this.shownCreateCallPlayers.size()); i++) {
                var player = this.shownCreateCallPlayers.get(i);
                if (this.stack != null && !isPlayerMe(player.profile)) {
                    var call = this.callButtonsHandheld[i - scroll];
                    call.visible = true;
                } else {
                    var toggle = this.toggleButtons[i - scroll];
                    toggle.setActive(player.added);
                    toggle.setLocked(player.locked);
                    toggle.visible = true;
                }
            }
        } else if (this.page == HologramScreen.Page.JOIN_CALL) {
            for (int i = scroll; i < Math.min(scroll + 3, this.shownJoinCallPlayers.size()); i++) {
                var player = this.shownJoinCallPlayers.get(i);
                var count = this.playerCountWidgets[i - scroll];
                count.visible = true;
                count.setPlayers(player.players);
                this.callButtons[i - scroll].visible = true;
                this.callButtons[i + 3 - scroll].visible = true;
            }
        } else {
            for (int i = scroll; i < Math.min(scroll + 3, this.shownSearchedPlayer.size()); i++) {
                this.checkButtons[i - scroll].visible = Minecraft.getInstance().level != null && getSoloRenderName().equals(this.shownSearchedPlayer.get(i - scroll).profile.getName());
                this.checkButtons[i + 3 - scroll].visible = true;
            }
        }
    }

    public void handleNetworkPacket(List<CallData.Call> playerCalls) {
        if (this.minecraft.player != null) {
            this.listedJoinCallPlayers.clear();
            var connection = this.minecraft.player.connection;
            playerCalls.forEach(call -> {
                var info = connection.getPlayerInfo(call.owner);
                if (info != null) this.listedJoinCallPlayers.add(MenuData.ofJoin(info.getProfile(), call.callID, playerNames(call.callers, connection::getPlayerInfo)));
            });
        }

        this.search(this.lastSearch);
    }

    private List<Component> playerNames(Map<UUID, CallData.Callee> callees, Function<UUID, PlayerInfo> mapper) {
        return callees.values().stream().map(callee -> {
            if (mapper.apply(callee.playerUUID) == null) return Component.literal("Offline Player");
            return (Component) Component.literal(mapper.apply(callee.playerUUID).getProfile().getName());
        }).toList();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (this.page == HologramScreen.Page.CREATE_CALL) renderScreen(graphics, this.shownCreateCallPlayers, "Add Players to Call", "No Players Found", "You", "", 0);
        else if (this.page == HologramScreen.Page.JOIN_CALL) renderScreen(graphics, this.shownJoinCallPlayers, "Joinable Holo Calls", "No Calls Found", "My Call", "'s Call", -8);
        else renderScreen(graphics, this.shownSearchedPlayer, "Find Player Screen", "No Player Found", "My Skin", "", 0);
    }

    private void renderScreen(GuiGraphics graphics, List<MenuData> menu, String topMessage, String emptyList, String mePrefix, String suffix, int offset) {
        var width = 220;

        graphics.drawCenteredString(this.minecraft.font, Component.literal(topMessage), x + width / 2, y, -1);
        var y = this.y + 11;
        if (menu.isEmpty()) graphics.drawCenteredString(this.minecraft.font, Component.literal(emptyList).withStyle(ChatFormatting.BOLD, ChatFormatting.RED), x + width / 2, y + 16, -1);
        for (int i = this.scroll; i < Math.min(this.scroll + 3, menu.size()); i++) {
            var profile = menu.get(i).profile;
            if (profile != null) {
                var me = this.isPlayerMe(profile);
                var minY = y + this.height * (i - this.scroll);
                var name = me ? mePrefix : (profile.getName() + suffix);

                GraphicsUtil.fillRect(graphics, this.x, minY, width, this.height, ColorUtil.packArgb(74, 74, 74, 255));
                PlayerFaceRenderer.draw(graphics, minecraft.getSkinManager().getInsecureSkin(profile), this.x + 4, minY + 4, 24);
                graphics.drawString(this.minecraft.font, Component.literal(name).withStyle(ChatFormatting.BOLD).withStyle(me ? ChatFormatting.GOLD : ChatFormatting.WHITE), this.x + 34, minY + 12 + offset, -1);
            }
        }
    }

    private boolean isPlayerMe(@Nullable GameProfile profile) {
        return this.minecraft.player != null && profile != null && profile.getId().equals(this.minecraft.player.getUUID());
    }

    public void setPage(HologramScreen.Page page) {
        this.page = page;
        this.lastSearch = "";

        this.refresh();
    }

    public void search(String search) {
        this.lastSearch = search;
        this.searchList(search, this.shownCreateCallPlayers, this.listedCreateCallPlayers);
        this.searchList(search, this.shownJoinCallPlayers, this.listedJoinCallPlayers);
        this.searchList(search, this.shownSearchedPlayer, this.listedSearchedPlayer);
        this.updateMenu(0);
    }

    private void searchList(String search, List<MenuData> searchList, List<MenuData> players) {
        searchList.clear();
        if (search == null || search.isEmpty()) searchList.addAll(players);
        else searchList.addAll(players.stream().filter(s -> s.profile.getName().toLowerCase().contains(search.toLowerCase())).toList());
    }

    public void refresh() {
        this.refreshPlayerList();
        this.search(this.lastSearch);
    }

    private String getSoloRenderName() {
        if (this.screen.getHoloPos() != null && Minecraft.getInstance().level != null && Minecraft.getInstance().level.getBlockEntity(this.screen.getHoloPos()) instanceof HoloProjectorBlockEntity be) return be.getSoloRender();
        return this.screen.getStack() != null ? HologramComponent.getOrCreate(this.screen.getStack()).soloRender() : "";
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        this.scroller.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        if (this.page == HologramScreen.Page.CREATE_CALL) return this.listedCreateCallPlayers.size() > 3;
        if (this.page == HologramScreen.Page.JOIN_CALL) return this.shownJoinCallPlayers.size() > 3;
        return this.shownSearchedPlayer.size() > 3;
    }

    @Override
    public void setFocused(boolean focused) {
    }

    @Override
    public boolean isFocused() {
        return false;
    }

    @Override
    public @NotNull NarratableEntry.NarrationPriority narrationPriority() {
        return NarratableEntry.NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) { }

    public List<CallData.Callee> getCallMembers() {
        return this.shownCreateCallPlayers.stream().filter(p -> !isPlayerMe(p.profile) && p.added).map(p -> new CallData.Callee(p.profile.getId())).toList();
    }

    record MenuData(GameProfile profile, UUID callID, List<Component> players, boolean added, boolean locked) {
        public static MenuData ofCreate(GameProfile profile, boolean isMe) {
            return new MenuData(profile, null, List.of(), isMe, isMe);
        }

        public static MenuData ofJoin(GameProfile profile, UUID callID, List<Component> players) {
            return new MenuData(profile, callID, players, false, false);
        }
    }
}
