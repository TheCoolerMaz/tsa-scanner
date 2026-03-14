package com.tsascanner.ecs.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class PositionComponent implements Component {
    public static final ComponentMapper<PositionComponent> Mapper = ComponentMapper.getFor(PositionComponent.class);

    public float x = 0.0f;
    public float y = 0.0f;

    public PositionComponent() {}

    public PositionComponent(float x, float y) {
        this.x = x;
        this.y = y;
    }
}
