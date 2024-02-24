package net.swofty.loader;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.minestom.server.MinecraftServer;
import net.minestom.server.extras.velocity.VelocityProxy;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.Configuration;
import net.swofty.commons.ServerType;
import net.swofty.proxyapi.ProxyAPI;
import net.swofty.proxyapi.ProxyService;
import net.swofty.proxyapi.redis.RedisMessage;
import net.swofty.service.protocol.ProtocolSpecification;
import net.swofty.types.generic.SkyBlockConst;
import net.swofty.types.generic.SkyBlockGenericLoader;
import net.swofty.types.generic.SkyBlockTypeLoader;
import net.swofty.types.generic.protocol.ProtocolPingSpecification;
import net.swofty.types.generic.redis.RedisHasIslandLoaded;
import net.swofty.types.generic.redis.RedisPing;
import net.swofty.types.generic.redis.RedisRefreshCoopData;
import net.swofty.types.generic.redis.RedisRunEvent;
import org.json.JSONObject;
import org.reflections.Reflections;
import org.tinylog.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class SkyBlock {
    @Getter
    @Setter
    private static UUID serverUUID;
    @Getter
    @Setter
    private static SkyBlockTypeLoader typeLoader;

    @SneakyThrows
    public static void main(String[] args) {
        if (args.length == 0 || !ServerType.isServerType(args[0])) {
            Logger.error("Please specify a server type.");
            Arrays.stream(ServerType.values()).forEach(serverType -> Logger.error(serverType.name()));
            System.exit(0);
            return;
        }
        ServerType serverType = ServerType.valueOf(args[0].toUpperCase());
        long startTime = System.currentTimeMillis();

        /**
         * Initialize TypeLoader
         */
        Reflections reflections = new Reflections("net.swofty.type");
        Set<Class<? extends SkyBlockTypeLoader>> subTypes = reflections.getSubTypesOf(SkyBlockTypeLoader.class);
        if (subTypes.isEmpty()) {
            Logger.error("No TypeLoader found!");
            System.exit(0);
            return;
        }
        typeLoader = subTypes.stream().filter(clazz -> {
            try {
                ServerType type = clazz.getDeclaredConstructor().newInstance().getType();
                Logger.info("Found TypeLoader: " + type.name());
                return type == serverType;
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                return false;
            }
        }).findFirst().orElse(null).getDeclaredConstructor().newInstance();

        /**
         * Initialize the server
         */
        MinecraftServer minecraftServer = MinecraftServer.init();
        serverUUID = UUID.randomUUID();

        new SkyBlockGenericLoader(typeLoader).initialize(minecraftServer);
        typeLoader.onInitialize(minecraftServer);

        /**
         * Initialize Proxy support
         */
        Logger.info("Initializing proxy support...");
        ArrayList<String> requiredChannels = new ArrayList<>(Arrays.asList(
                "proxy-online",
                "server-initialized",
                "server-name",
                "player-handler"
        ));
        Reflections protocolSpecifications = new Reflections("net.swofty.types.generic.protocol");
        Set<Class<? extends ProtocolSpecification>> subTypesOfProtocol = protocolSpecifications.getSubTypesOf(ProtocolSpecification.class);
        subTypesOfProtocol.forEach(protocol -> {
            try {
                ProtocolSpecification specification = protocol.getDeclaredConstructor().newInstance();
                requiredChannels.add(specification.getEndpoint());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                e.printStackTrace();
            }
        });
        ProxyAPI proxyAPI = new ProxyAPI(Configuration.get("redis-uri"), serverUUID, requiredChannels.toArray(new String[0]));
        proxyAPI.registerProxyToClient("ping", RedisPing.class);
        proxyAPI.registerProxyToClient("run-event", RedisRunEvent.class);
        proxyAPI.registerProxyToClient("refresh-data", RedisRefreshCoopData.class);
        proxyAPI.registerProxyToClient("has-island", RedisHasIslandLoaded.class);
        proxyAPI.start();
        VelocityProxy.enable(Configuration.get("velocity-secret"));

        /**
         * Ensure all services are running
         */
        typeLoader.getRequiredServices().forEach(serviceType -> {
            new ProxyService(serviceType).isOnline(new ProtocolPingSpecification()).thenAccept(online -> {
                if (!online) {
                    Logger.error("Service " + serviceType.name() + " is not online!");
                }
            });
        });
        typeLoader.afterInitialize(minecraftServer);

        /**
         * Start the server
         */
        MinecraftServer.setBrandName("SkyBlock");

        CompletableFuture<Integer> startServer = new CompletableFuture<>();
        startServer.whenComplete((port, throwable) -> {
            minecraftServer.start(Configuration.get("host-name"), port);

            long endTime = System.currentTimeMillis();
            Logger.info("Started server on port " + port + " in " + (endTime - startTime) + "ms");
            Logger.info("Server Type: " + serverType.name());
            Logger.info("Internal ID: " + serverUUID.toString());

            RedisMessage.sendMessageToProxy(
                    "server-name", "",
                    SkyBlockConst::setServerName);
            checkProxyConnected(MinecraftServer.getSchedulerManager());
        });
        new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (startServer.isDone()) return;
            Logger.error("Couldn't connect to proxy. Shutting down...");
            System.exit(0);
        }).start();

        RedisMessage.sendMessageToProxy(
                "server-initialized",
                new JSONObject().put("type", serverType.name()).toString(),
                (response) -> startServer.complete(Integer.parseInt(response)));
    }

    private static void checkProxyConnected(Scheduler scheduler) {
        scheduler.submitTask(() -> {
            AtomicBoolean responded = new AtomicBoolean(false);

            RedisMessage.sendMessageToProxy("proxy-online", "online", (response) -> {
                if (response.equals("true"))
                    responded.set(true);
            });

            scheduler.scheduleTask(() -> {
                if (!responded.get()) {
                    Logger.error("Proxy did not respond to alive check. Shutting down...");
                    System.exit(0);
                }
            }, TaskSchedule.tick(4), TaskSchedule.stop());

            return TaskSchedule.seconds(1);
        });
    }
}