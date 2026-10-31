package ru.svyaton.bookpaste;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.fml.ModList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import java.lang.reflect.Proxy;

@EventBusSubscriber(modid = "bookpaste", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class BookPasteClient {
    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        ModList.get().getModContainerById("bookpaste").orElseThrow().registerExtensionPoint(
                IConfigScreenFactory.class, createScreenFactory());
    }

    private static IConfigScreenFactory createScreenFactory() {
        return (IConfigScreenFactory) Proxy.newProxyInstance(IConfigScreenFactory.class.getClassLoader(),
                new Class<?>[]{IConfigScreenFactory.class}, (proxy, method, arguments) -> switch (method.getName()) {
                    case "createScreen" -> new BookPasteConfigScreen((Screen) arguments[1]);
                    case "toString" -> "BookPaste settings";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == arguments[0];
                    default -> throw new UnsupportedOperationException(method.getName());
                });
    }

    @SubscribeEvent
    public static void keys(RegisterKeyMappingsEvent event) {
        event.register(BookPasteHotkeys.TOGGLE_FORMAT_MODE);
    }

    @EventBusSubscriber(modid = "bookpaste", value = Dist.CLIENT)
    public static final class Events {
        @SubscribeEvent
        public static void tick(ClientTickEvent.Post event) {
            Minecraft client = Minecraft.getInstance();
            BookPasteNetworkingClient.tick(client);
            BookPasteHotkeys.tick(client);
            ScribbleCompat.tick(client);
        }
    }
}
