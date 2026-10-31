package de.begleiter;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class CompanionCommands {
    private CompanionCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("begleiter")
                .then(Commands.literal("spawn").executes(c -> spawn(c.getSource())))
                .then(Commands.literal("weg").executes(c -> despawn(c.getSource())))
                .then(Commands.literal("skin")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(c -> skin(c.getSource(), StringArgumentType.getString(c, "name")))))
                .then(Commands.literal("sag")
                        .then(Commands.argument("text", StringArgumentType.greedyString())
                                .executes(c -> say(c.getSource(), StringArgumentType.getString(c, "text"))))));
        // Kurzbefehl: /b Hallo!
        d.register(Commands.literal("b")
                .then(Commands.argument("text", StringArgumentType.greedyString())
                        .executes(c -> say(c.getSource(), StringArgumentType.getString(c, "text")))));
    }

    private static int spawn(CommandSourceStack src) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer p = src.getPlayerOrException();
        BegleiterEntity b = CompanionManager.spawn(p);
        p.sendSystemMessage(Component.literal(b == null
                ? "Begleiter konnte nicht erscheinen."
                : "<Begleiter> Hallo! Ich bin da. Schreib mit /b <text> mit mir."));
        return 1;
    }

    private static int despawn(CommandSourceStack src) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        CompanionManager.despawn(src.getPlayerOrException());
        src.getPlayerOrException().sendSystemMessage(Component.literal("<Begleiter> Bis bald!"));
        return 1;
    }

    private static int skin(CommandSourceStack src, String name) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer p = src.getPlayerOrException();
        BegleiterEntity b = CompanionManager.get(p);
        if (b == null) {
            p.sendSystemMessage(Component.literal("Du hast keinen Begleiter. Nutze /begleiter spawn."));
            return 0;
        }
        if (!name.matches("[A-Za-z0-9_-]{1,32}")) {
            p.sendSystemMessage(Component.literal("Skin-Name: nur Buchstaben, Zahlen, _ und -."));
            return 0;
        }
        b.setSkin(name);
        p.sendSystemMessage(Component.literal("<Begleiter> Neuer Look: " + name
                + " (Datei config/begleiter/skins/" + name + ".png, sonst Standard-Skin)"));
        return 1;
    }

    private static int say(CommandSourceStack src, String text) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer p = src.getPlayerOrException();
        BegleiterEntity b = CompanionManager.get(p);
        if (b == null) {
            p.sendSystemMessage(Component.literal("Du hast keinen Begleiter. Nutze /begleiter spawn."));
            return 0;
        }
        // Nur der Spieler selbst sieht Frage und Antwort (kein Broadcast an andere).
        p.sendSystemMessage(Component.literal("<Du> " + text));
        p.sendSystemMessage(Component.literal("<Begleiter> " + b.replyTo(text)));
        return 1;
    }
}
