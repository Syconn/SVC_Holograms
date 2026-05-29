package mod.syconn.svc.client.screen.components;

import mod.syconn.svc.client.screen.HologramScreen;
import mod.syconn.svc.client.screen.components.buttons.CallButton;
import mod.syconn.svc.client.screen.components.buttons.ToggleButton;
import mod.syconn.svc.network.Network;
import mod.syconn.svc.network.packets.server.RequestHologramPacket;
import mod.syconn.svc.server.savedData.extra.CallData;
import mod.syconn.svc.utils.generic.ColorUtil;
import mod.syconn.svc.utils.generic.GraphicsUtil;
import mod.syconn.svc.utils.interfaces.IWidgetComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class CallMenuWidget implements IWidgetComponent {

    private final List<MenuData> listedCreateCallPlayers = new ArrayList<>();
    private final List<MenuData> listedJoinCallPlayers = new ArrayList<>();
    private final List<MenuData> shownCreateCallPlayers = new ArrayList<>();
    private final List<MenuData> shownJoinCallPlayers = new ArrayList<>();
    private final ToggleButton[] toggleButtons = new ToggleButton[3];
    private final CallButton[] callButtonsHandheld = new CallButton[3];
    private final PlayerCountWidget[] playerCountWidgets = new PlayerCountWidget[3];
    private final CallButton[] callButtons = new CallButton[6];
    private final Minecraft minecraft = Minecraft.getInstance();
    private final int color = ColorUtil.packArgb(74, 74, 74, 255);
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
            this.callButtonsHandheld[v] = (CallButton) widgets.apply(new CallButton(this.x + 180, this.y + 17 + row, CallButton.Type.START, "Begin Call", b -> this.callHandheldPressed(b, v)));
            this.playerCountWidgets[v] = (PlayerCountWidget) widgets.apply(new PlayerCountWidget(this.x + 195, this.y + 28 + row));
            this.callButtons[v] = (CallButton) widgets.apply(new CallButton(this.x + 34, this.y + 26 + row, 0.75f, CallButton.Type.START, "Join Call", b -> callPressed((CallButton) b, v)));
            this.callButtons[v + 3] = (CallButton) widgets.apply(new CallButton(this.x + 100, this.y + 26 + row, 0.75f, CallButton.Type.END, "Decline Call", b -> callPressed((CallButton) b, v)));
        }

        this.scroller = (ScrollerWidget) widgets.apply(new ScrollerWidget(x + 207, y + 11, 91, this.listedCreateCallPlayers.size() - 3, w -> true, this::updateMenu));
    }

    private void toggled(ToggleButton button, int i) {
        var player = this.shownCreateCallPlayers.get(this.scroll + i);
        this.shownCreateCallPlayers.set(this.scroll + i, new MenuData(player.info, player.callID, List.of(), !button.active(), player.locked));
    }

    private void callHandheldPressed(Button button, int i) {
//        var holo = HologramData.HologramTag.getOrCreate(this.stack); TODO Incomplete
//        var player = this.shownCreateCallPlayers.get(this.scroll + i);
//        this.screen.createCall(ListUtil.append(this.screen.getCaller(), List.of(new HologramNetwork.Caller(player.info.getProfile().getId(), null, null))));
    }

    private void callPressed(CallButton button, int i) {
        var uuid = this.shownJoinCallPlayers.get(this.scroll + i).callID;
        if (button.getType() == CallButton.Type.END) this.screen.leaveCall(uuid);
        else this.screen.joinCall(uuid);

        refresh();
    }

    private void refreshPlayerList() {
        if (this.minecraft.player != null) {
            this.listedCreateCallPlayers.clear();

            if (this.page == HologramScreen.Page.CREATE_CALL) {
                var connection = this.minecraft.player.connection;
                var uuids = connection.getOnlinePlayerIds();
                uuids.forEach(uuid -> this.listedCreateCallPlayers.add(MenuData.ofCreate(connection.getPlayerInfo(uuid), isPlayerMe(connection.getPlayerInfo(uuid)))));
            } else Network.CHANNEL.sendToServer(new RequestHologramPacket());
        }
    }

    private void updateMenu(int scroll) {
        this.scroll = scroll;
        this.scroller.updateSize(this.shownCreateCallPlayers.size() - 3);

        Arrays.stream(this.toggleButtons).forEach(b -> b.visible = false);
        Arrays.stream(this.callButtonsHandheld).forEach(b -> b.visible = false);
        Arrays.stream(this.playerCountWidgets).forEach(b -> b.visible = false);
        Arrays.stream(this.callButtons).forEach(b -> b.visible = false);

        if (this.page == HologramScreen.Page.CREATE_CALL) {
            for (int i = scroll; i < Math.min(scroll + 3, this.shownCreateCallPlayers.size()); i++) {
                var player = this.shownCreateCallPlayers.get(i);
                if (this.stack != null && !isPlayerMe(player.info)) {
                    var call = this.callButtonsHandheld[i - scroll];
                    call.visible = true;
                } else {
                    var toggle = this.toggleButtons[i - scroll];
                    toggle.setActive(player.added);
                    toggle.setLocked(player.locked);
                    toggle.visible = true;
                }
            }
        } else {
            for (int i = scroll; i < Math.min(scroll + 3, this.shownJoinCallPlayers.size()); i++) {
                var player = this.shownJoinCallPlayers.get(i);
                var count = this.playerCountWidgets[i - scroll];
                count.visible = true;
                count.setPlayers(player.players);
                this.callButtons[i].visible = true;
                this.callButtons[i + 3].visible = true;
            }
        }
    }

    public void handleNetworkPacket(List<CallData.Call> playerCalls) {
        if (this.minecraft.player != null) {
            this.listedJoinCallPlayers.clear();
            var connection = this.minecraft.player.connection;
            playerCalls.forEach(call -> this.listedJoinCallPlayers.add(MenuData.ofJoin(connection.getPlayerInfo(call.owner), call.callID, playerNames(call.callers, connection::getPlayerInfo))));
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
        else renderScreen(graphics, this.shownJoinCallPlayers, "Joinable Holo Calls", "No Calls Found", "My Call", "'s Call", -8);
    }

    private void renderScreen(GuiGraphics graphics, List<MenuData> menu, String topMessage, String emptyList, String mePrefix, String suffix, int offset) {
        var width = 220;

        graphics.drawCenteredString(this.minecraft.font, Component.literal(topMessage), x + width / 2, y, -1);

        var y = this.y + 11;
        if (menu.isEmpty()) graphics.drawCenteredString(this.minecraft.font, Component.literal(emptyList).withStyle(ChatFormatting.BOLD, ChatFormatting.RED), x + width / 2, y + 16, -1);
        for (int i = this.scroll; i < Math.min(this.scroll + 3, menu.size()); i++) {
            var info = menu.get(i).info;
            if (info != null) {
                var me = this.isPlayerMe(info);
                var minY = y + this.height * (i - this.scroll);
                var name = me ? mePrefix : (info.getProfile().getName() + suffix);

                GraphicsUtil.fillRect(graphics, this.x, minY, width, this.height, this.color);
                PlayerFaceRenderer.draw(graphics, info.getSkinLocation(), this.x + 4, minY + 4, 24);
                graphics.drawString(this.minecraft.font, Component.literal(name).withStyle(ChatFormatting.BOLD).withStyle(me ? ChatFormatting.GOLD : ChatFormatting.WHITE), this.x + 34, minY + 12 + offset, -1);
            }
        }
    }
    
    private boolean isPlayerMe(@Nullable PlayerInfo info) {
        return this.minecraft.player != null && info != null && info.getProfile().getId().equals(this.minecraft.player.getUUID());
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
        this.updateMenu(0);
    }

    private void searchList(String search, List<MenuData> searchList, List<MenuData> players) {
        searchList.clear();
        if (search == null || search.isEmpty()) searchList.addAll(players);
        else searchList.addAll(players.stream().filter(s -> s.info.getProfile().getName().toLowerCase().contains(search.toLowerCase())).toList());
    }

    public void refresh() {
        this.refreshPlayerList();
        this.search(this.lastSearch);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        this.scroller.mouseScrolled(mouseX, mouseY, delta);
        return this.listedCreateCallPlayers.size() > 3;
    }

    @Override
    public void setFocused(boolean focused) {}

    @Override
    public boolean isFocused() {
        return false;
    }

    @Override
    public @NotNull NarratableEntry.NarrationPriority narrationPriority() {
        return NarratableEntry.NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {}

    public List<CallData.Callee> getCallMembers() {
        return this.shownCreateCallPlayers.stream().filter(p -> !isPlayerMe(p.info) && p.added).map(p -> new CallData.Callee(p.info.getProfile().getId())).toList();
    }

    record MenuData(PlayerInfo info, UUID callID, List<Component> players, boolean added, boolean locked) {
        public static MenuData ofCreate(PlayerInfo info, boolean isMe) {
            return new MenuData(info, null, List.of(), isMe, isMe);
        }

        public static MenuData ofJoin(PlayerInfo info, UUID callID, List<Component> players) {
            return new MenuData(info, callID, players, false, false);
        }
    }
}
