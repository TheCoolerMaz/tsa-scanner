package com.tsascanner.ecs.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.math.Vector2;

public class ColliderComponent implements Component {
    public static final ComponentMapper<ColliderComponent> Mapper = ComponentMapper.getFor(ColliderComponent.class);

    public float width = 0.0f;
    public float height = 0.0f;
    public Vector2 offset = new Vector2();
}
