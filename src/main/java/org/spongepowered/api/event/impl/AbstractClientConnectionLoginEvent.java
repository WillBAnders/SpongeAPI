/*
 * This file is part of SpongeAPI, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.api.event.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.util.Transform;
import org.spongepowered.api.util.annotation.eventgen.UseField;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public abstract class AbstractClientConnectionLoginEvent extends AbstractEvent implements ClientConnectionEvent.Login {

    @UseField private Transform toTransform;
    @UseField private World toWorld;

    @Override
    public void setLocation(World world, Transform transform) {
        this.toWorld = checkNotNull(world, "world");
        this.toTransform = checkNotNull(transform, "transform");
    }

    @Override
    public void setLocation(Location location) {
        checkNotNull(location, "location");
        this.toWorld = location.getWorld();
        this.toTransform = this.toTransform.withPosition(location.getPosition());
    }

    @Override
    public void setRotation(Vector3d rotation) {
        checkNotNull(rotation, "rotation");
        this.toTransform = this.toTransform.withRotation(rotation);
    }
}