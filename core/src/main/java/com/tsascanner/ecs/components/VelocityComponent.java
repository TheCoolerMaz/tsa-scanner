package com.tsascanner.ecs.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class VelocityComponent implements Component {
    public static final ComponentMapper<VelocityComponent> Mapper = ComponentMapper.getFor(VelocityComponent.class);

    public float dx = 0.0f;
    public float dy = 0.0f;

    public VelocityComponent() {}

    public VelocityComponent(float dx, float dy) {
        this.dx = dx;
        this.dy = dy;
    }
}
