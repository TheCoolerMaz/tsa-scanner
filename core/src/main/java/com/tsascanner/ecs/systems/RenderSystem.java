package com.tsascanner.ecs.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.tsascanner.ecs.components.PositionComponent;
import com.tsascanner.ecs.components.SpriteComponent;

public class RenderSystem extends IteratingSystem {
    private SpriteBatch batch;
    private ComponentMapper<PositionComponent> pm;
    private ComponentMapper<SpriteComponent> sm;

    public RenderSystem(SpriteBatch batch) {
        super(Family.all(PositionComponent.class, SpriteComponent.class).get());
        this.batch = batch;
        pm = PositionComponent.Mapper;
        sm = SpriteComponent.Mapper;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        PositionComponent position = pm.get(entity);
        SpriteComponent sprite = sm.get(entity);

        if (sprite.region == null) return;

        batch.draw(sprite.region,
                   position.x - sprite.origin.x,
                   position.y - sprite.origin.y,
                   sprite.origin.x,
                   sprite.origin.y,
                   sprite.region.getRegionWidth(),
                   sprite.region.getRegionHeight(),
                   sprite.scale,
                   sprite.scale,
                   sprite.rotation);
    }

    public void setBatch(SpriteBatch batch) {
        this.batch = batch;
    }
}
