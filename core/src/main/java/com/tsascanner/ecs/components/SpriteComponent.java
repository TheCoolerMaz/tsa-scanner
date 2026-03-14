package com.tsascanner.ecs.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class SpriteComponent implements Component {
    public static final ComponentMapper<SpriteComponent> Mapper = ComponentMapper.getFor(SpriteComponent.class);

    public TextureRegion region;
    public Vector2 origin = new Vector2();
    public float scale = 1.0f;
    public float rotation = 0.0f;
}
