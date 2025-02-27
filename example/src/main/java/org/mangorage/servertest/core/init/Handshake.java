package org.mangorage.servertest.core.init;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.extras.bungee.BungeeCordProxy;
import net.minestom.server.listener.preplay.HandshakeListener;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.packet.client.handshake.ClientHandshakePacket;
import net.minestom.server.network.packet.server.login.LoginDisconnectPacket;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.network.player.PlayerSocketConnection;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Handshake {

    private final static Logger LOGGER = LoggerFactory.getLogger(HandshakeListener.class);

    /**
     * Text sent if a player tries to connect with an invalid version of the client
     */
    private static final Component INVALID_VERSION_TEXT = Component.text("Invalid Version, please use " + MinecraftServer.VERSION_NAME, NamedTextColor.RED);

    /**
     * Indicates that a BungeeGuard authentication was invalid due to missing, multiple, or invalid tokens.
     */
    private static final Component INVALID_BUNGEE_FORWARDING = Component.text("Invalid connection, please connect through the BungeeCord proxy. If you believe this is an error, contact a server administrator.", NamedTextColor.RED);

    private static final Component TRANSFER_NOT_ALLOWED = Component.text("Transfers are not allowed on this server.", NamedTextColor.RED);
    private static final Component DIRECT_CONNECTIONS_NOT_ALLOWED = Component.text("Direct connections are not allowed on this server.", NamedTextColor.RED);

    public static void listener(@NotNull ClientHandshakePacket packet, @NotNull PlayerConnection connection) {
        String address = packet.serverAddress();
        switch (packet.intent()) {
            case STATUS -> connection.setConnectionState(ConnectionState.STATUS);
            case LOGIN -> {
                connection.setConnectionState(ConnectionState.LOGIN);
                if (packet.protocolVersion() != MinecraftServer.PROTOCOL_VERSION) {
                    // Incorrect client version
                    disconnect(connection, INVALID_VERSION_TEXT);
                }

                /**
                if (!MinecraftServer.getConnectionRule().isAllowed(ClientHandshakePacket.Intent.LOGIN)) {
                    // Direct connections not allowed
                    disconnect(connection, DIRECT_CONNECTIONS_NOT_ALLOWED);
                }
                 **/

                // Bungee support (IP forwarding)
                String bungeeAddress = bungeeConnect(address, connection);

                if (bungeeAddress == null) {
                    return;
                }

                address = bungeeAddress;
            }
            case TRANSFER -> {
                System.out.println("Transfer");
                connection.setConnectionState(ConnectionState.LOGIN);
                if (packet.protocolVersion() != MinecraftServer.PROTOCOL_VERSION) {
                    // Incorrect client version
                    disconnect(connection, INVALID_VERSION_TEXT);
                }

                /**
                if (!MinecraftServer.getConnectionRule().isAllowed(ClientHandshakePacket.Intent.TRANSFER)) {
                    // Transfer not allowed
                    disconnect(connection, INVALID_VERSION_TEXT);
                }
                 **/

                // Bungee support (IP forwarding)
                String bungeeAddress = bungeeConnect(address, connection);

                if (bungeeAddress == null) {
                    return;
                }

                address = bungeeAddress;
            }
            default -> {
                // Unexpected error
                System.out.println("Eh?");
            }
        }

        if (connection instanceof PlayerSocketConnection) {
            // Give to the connection the server info that the client used
            ((PlayerSocketConnection) connection).refreshServerInformation(address, packet.serverPort(), packet.protocolVersion());
        }
    }

    private static String bungeeConnect(String address, @NotNull PlayerConnection connection) {
        if (BungeeCordProxy.isEnabled() && connection instanceof PlayerSocketConnection socketConnection) {
            final String[] split = address.split("\00");

            if (split.length == 3 || split.length == 4) {
                boolean hasProperties = split.length == 4;
                if (BungeeCordProxy.isBungeeGuardEnabled() && !hasProperties) {
                    bungeeDisconnect(socketConnection);
                    return address;
                }

                address = split[0];

                final SocketAddress socketAddress = new InetSocketAddress(split[1],
                        ((InetSocketAddress) connection.getRemoteAddress()).getPort());
                socketConnection.setRemoteAddress(socketAddress);

                UUID playerUuid = UUID.fromString(
                        split[2]
                                .replaceFirst(
                                        "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"
                                )
                );

                List<GameProfile.Property> properties = new ArrayList<>();
                if (hasProperties) {
                    boolean foundBungeeGuardToken = false;
                    final String rawPropertyJson = split[3];
                    final JsonArray propertyJson = JsonParser.parseString(rawPropertyJson).getAsJsonArray();
                    for (JsonElement element : propertyJson) {
                        final JsonObject jsonObject = element.getAsJsonObject();
                        final JsonElement name = jsonObject.get("name");
                        final JsonElement value = jsonObject.get("value");
                        final JsonElement signature = jsonObject.get("signature");
                        if (name == null || value == null) continue;

                        final String nameString = name.getAsString();
                        final String valueString = value.getAsString();
                        final String signatureString = signature == null ? null : signature.getAsString();

                        if (BungeeCordProxy.isBungeeGuardEnabled() && nameString.equals("bungeeguard-token")) {
                            if (foundBungeeGuardToken || !BungeeCordProxy.isValidBungeeGuardToken(valueString)) {
                                bungeeDisconnect(socketConnection);
                                return null;
                            }

                            foundBungeeGuardToken = true;
                        }

                        properties.add(new GameProfile.Property(nameString, valueString, signatureString));
                    }

                    if (BungeeCordProxy.isBungeeGuardEnabled() && !foundBungeeGuardToken) {
                        bungeeDisconnect(socketConnection);
                        return null;
                    }
                }

                final GameProfile gameProfile = new GameProfile(playerUuid, "test", properties);
                socketConnection.UNSAFE_setProfile(gameProfile);
            } else {
                bungeeDisconnect(socketConnection);
                return null;
            }
        }

        return address;
    }

    private static void disconnect(@NotNull PlayerConnection connection, @NotNull Component reason) {
        connection.sendPacket(new LoginDisconnectPacket(reason));
        connection.disconnect();
    }

    private static void bungeeDisconnect(@NotNull PlayerConnection connection) {
        LOGGER.warn("{} tried to log in without valid BungeeGuard forwarding information.", connection.getIdentifier());
        disconnect(connection, INVALID_BUNGEE_FORWARDING);
    }

}
