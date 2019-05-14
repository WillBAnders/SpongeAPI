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
package org.spongepowered.api.command;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.SystemSubject;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.Location;
import org.spongepowered.math.vector.Vector3d;

import java.util.Optional;

/**
 * The {@link CommandCause} represents the {@link Cause} of a command, and
 * also contains utility methods to obtain key information about said cause.
 *
 * <p>Command consumers are under no obligation to use the utility methods as
 * all methods obtain their information from the {@link Cause}.They do, however,
 * provide hints as to what the implementation will select.</p>
 *
 * <p>No method on this interface, apart from {@link #getCause()}, should
 * be taken a guarantee of what may be present, however, they indicate what
 * typically would be of interest to command API consumers.</p>
 */
public interface CommandCause {

    /**
     * Creates a {@link CommandCause} from the provided {@link Cause}
     *
     * @param cause The {@link Cause}
     * @return The {@link CommandCause}
     */
    static CommandCause of(Cause cause) {
        return Sponge.getRegistry().requireFactory(Factory.class).create(cause);
    }

    /**
     * Gets the {@link Cause} of the command invocation.
     *
     * @return The cause of the invocation.
     */
    Cause getCause();

    /**
     * Get the {@link Subject} that will be selected for permission checks
     * during command execution (by default).
     *
     * <p>The {@link Subject} will be selected in the following way from the
     * {@link Cause} in {@link #getCause()}:</p>
     *
     * <ul>
     *    <li>The {@link EventContextKeys#SUBJECT}, if any</li>
     *    <li>The <strong>first</strong> {@link Subject} in the {@link Cause}
     *    </li>
     *    <li>The {@link SystemSubject} if no subject exists within the cause
     *    </li>
     * </ul>
     *
     * <p><strong>Note:</strong> while it might be tempting to use this as the
     * invoker of the command, the {@link Cause#root()} and this might be
     * different. Command executors should generally use the root of the
     * {@link Cause} as the target of their command.</p>
     *
     * @return The {@link Subject} responsible, if any.
     */
    default Subject getSubject() {
        return getCause().getContext()
                .get(EventContextKeys.SUBJECT)
                .orElseGet(() -> getCause().first(Subject.class).orElseGet(Sponge::getSystemSubject));
    }

    /**
     * Gets the {@link MessageReceiver} that should be the target for any
     * messages sent by the command (by default).
     *
     * <p>The {@link MessageReceiver} will be selected in the following way
     * from the {@link Cause} in {@link #getCause()}:</p>
     *
     * <ul>
     *    <li>The {@link EventContextKeys#MESSAGE_TARGET}, if any</li>
     *    <li>A message channel containing the <strong>first</strong>
     *    {@link MessageReceiver} in the {@link Cause}</li>
     *    <li>The SystemSubject {@link MessageReceiver}</li>
     * </ul>
     *
     * <p>Note that this returns a {@link MessageReceiver} and it may not what
     * may be thought of as a traditional entity executing the command.
     * For the object that invoked the command, check the
     * {@link Cause#root()} of the {@link #getCause()}.</p>
     *
     * @return The {@link MessageReceiver} to send any messages to.
     */
    default MessageReceiver getMessageReceiver() {
        return getCause().getContext()
                .get(EventContextKeys.MESSAGE_TARGET)
                .orElseGet(() -> getCause().first(MessageReceiver.class).orElseGet(Sponge::getSystemSubject));
    }

    /**
     * Gets the {@link Location} that this command is associated with.
     *
     * <p>The following are checked in order:
     *
     * <ul>
     *     <li>The {@link EventContextKeys#LOCATION}, if any</li>
     *     <li>{@link #getTargetBlock()}</li>
     *     <li>The {@link EventContextKeys#MESSAGE_TARGET}, if it is
     *     {@link Locatable}</li>
     *     <li>the location of the first locatable in the {@link Cause}</li>
     * </ul>
     *
     * @return The {@link Location}, if it exists
     */
    default Optional<Location> getLocation() {
        Cause cause = getCause();
        EventContext eventContext = cause.getContext();
        if (eventContext.containsKey(EventContextKeys.LOCATION)) {
            return eventContext.get(EventContextKeys.LOCATION);
        }

        Optional<Location> optionalLocation = getTargetBlock().flatMap(BlockSnapshot::getLocation);
        if (optionalLocation.isPresent()) {
            return optionalLocation;
        }

        return Optional.ofNullable(
                eventContext.get(EventContextKeys.MESSAGE_TARGET)
                       .filter(x -> x instanceof Locatable)
                        .map(x -> ((Locatable) x).getLocation())
                        .orElseGet(() -> cause.first(Locatable.class).map(Locatable::getLocation).orElse(null)));
    }

    /**
     * Gets the {@link Vector3d} rotation that this command is associated with.
     *
     * <p>The following are checked in order:
     *
     * <ul>
     *     <li>The {@link EventContextKeys#ROTATION}, if any</li>
     *     <li>The {@link EventContextKeys#MESSAGE_TARGET}, if it has a
     *     rotation</li>
     *     <li>the rotation of the first {@link Entity} in the {@link Cause}</li>
     * </ul>
     *
     * @return The {@link Vector3d} rotation, if it exists
     */
    default Optional<Vector3d> getRotation() {
        Cause cause = getCause();
        EventContext eventContext = cause.getContext();
        if (eventContext.containsKey(EventContextKeys.ROTATION)) {
            return eventContext.get(EventContextKeys.ROTATION);
        }

        return Optional.ofNullable(
                eventContext.get(EventContextKeys.MESSAGE_TARGET)
                        .filter(x -> x instanceof Entity)
                        .map(x -> ((Entity) x).getRotation())
                        .orElseGet(() -> cause.first(Entity.class).map(Entity::getRotation).orElse(null)));
    }

    /**
     * Returns the target block {@link Location}, if applicable.
     *
     * <p>The following are checked in order:
     *
     * <ul>
     *     <li>The {@link EventContextKeys#BLOCK_TARGET}, if any</li>
     *     <li>The first {@link BlockSnapshot} in the {@link Cause}</li>
     * </ul>
     *
     * @return The {@link BlockSnapshot} if applicable, or an empty optional.
     */
    default Optional<BlockSnapshot> getTargetBlock() {
        return Optional.ofNullable(getCause().getContext().get(EventContextKeys.BLOCK_TARGET)
                .orElseGet(() -> getCause().first(BlockSnapshot.class).orElse(null)));
    }

    /**
     * Creates instances of the {@link CommandCause}.
     */
    interface Factory {

        /**
         * Creates the {@link CommandCause} from the {@link Cause}
         *
         * @param cause The {@link Cause}
         * @return The {@link CommandCause}
         */
        CommandCause create(Cause cause);
    }

}
