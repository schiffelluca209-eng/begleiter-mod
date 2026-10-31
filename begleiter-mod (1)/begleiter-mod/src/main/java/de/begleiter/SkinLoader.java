package de.begleiter;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

/**
 * Laedt Skins aus  config/begleiter/skins/<name>.png  (64x64 PNG).
 * "default" oder eine fehlende Datei ergibt den mitgelieferten Skin.
 */
public final class SkinLoader {
    public static final Identifier DEFAULT =
            Identifier.fromNamespaceAndPath(BegleiterMod.MOD_ID, "textures/entity/begleiter.png");

    private static final Map<String, Identifier> LOADED = new HashMap<>();
    private static final Map<String, Long> MISSING_UNTIL = new HashMap<>();

    private SkinLoader() {}

    public static Path skinDir() {
        return FabricLoader.getInstance().getConfigDir().resolve("begleiter").resolve("skins");
    }

    public static Identifier get(String name) {
        if (name == null || name.isEmpty() || name.equals("default")) return DEFAULT;
        Identifier id = LOADED.get(name);
        if (id != null) return id;

        long now = System.currentTimeMillis();
        Long until = MISSING_UNTIL.get(name);
        if (until != null && now < until) return DEFAULT; // Datei fehlt: nur alle 3 Sekunden neu pruefen

        Path file = skinDir().resolve(name + ".png");
        if (!Files.isRegularFile(file)) {
            MISSING_UNTIL.put(name, now + 3000);
            return DEFAULT;
        }
        try (InputStream in = Files.newInputStream(file)) {
            NativeImage image = NativeImage.read(in);
            Identifier texId = Identifier.fromNamespaceAndPath(
                    BegleiterMod.MOD_ID, "skins/" + name.toLowerCase(Locale.ROOT));
            Minecraft.getInstance().getTextureManager()
                    .register(texId, new DynamicTexture(() -> texId.toString(), image));
            LOADED.put(name, texId);
            return texId;
        } catch (Exception e) {
            MISSING_UNTIL.put(name, now + 10000);
            return DEFAULT;
        }
    }
}
