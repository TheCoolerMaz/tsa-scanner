package com.tsascanner.ecs.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.tsascanner.ecs.components.PositionComponent;
import com.tsascanner.ecs.components.VelocityComponent;

public class MovementSystem extends IteratingSystem {
    private ComponentMapper<PositionComponent> pm;
    private ComponentMapper<VelocityComponent> vm;

    public MovementSystem() {
        super(Family.all(PositionComponent.class, VelocityComponent.class).get());

        pm = PositionComponent.Mapper;
        vm = VelocityComponent.Mapper;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        PositionComponent position = pm.get(entity);
        VelocityComponent velocity = vm.get(entity);

        position.x += velocity.dx * deltaTime;
        position.y += velocity.dy * deltaTime;
    }
}
