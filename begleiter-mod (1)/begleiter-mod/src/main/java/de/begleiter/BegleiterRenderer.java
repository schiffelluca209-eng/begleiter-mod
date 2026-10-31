package de.begleiter;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;

/** Zeichnet den Begleiter mit dem Spieler-Modell und dem gewaehlten Skin (64x64). */
public class BegleiterRenderer
        extends HumanoidMobRenderer<BegleiterEntity, BegleiterRenderer.State, HumanoidModel<BegleiterRenderer.State>> {

    public static class State extends HumanoidRenderState {
        public String skin = "default";
    }

    public BegleiterRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new HumanoidModel<>(ctx.bakeLayer(ModelLayers.PLAYER)), 0.5f);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(BegleiterEntity entity, State state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.skin = entity.getSkin();
    }

    @Override
    public Identifier getTextureLocation(State state) {
        return SkinLoader.get(state.skin);
    }
}
