package com.tsascanner.ecs.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class TagComponent implements Component {
    public static final ComponentMapper<TagComponent> Mapper = ComponentMapper.getFor(TagComponent.class);

    public String tag;

    public TagComponent(String tag) {
        this.tag = tag;
    }
}
