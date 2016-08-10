/*
Copyright (c) 2009-2016, Andrew M. Martin
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
conditions are met:

 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
   disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
   disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of Pandam nor the names of its contributors may be used to endorse or promote products derived from this
   software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/
package org.pandcorps.botsnbolts;

import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.impl.*;
import org.pandcorps.pandax.tile.*;

public class ShootableDoor extends Panctor implements StepListener, CollisionListener {
    private final static Panple min = new FinPanple2(-12, 0);
    private final static Panple max = new FinPanple2(12, 64);
    private final static DoorDisplay display = new DoorDisplay();
    private final int x;
    private final int y;
    private final int doorX;
    private ShootableDoorDefinition def = null;
    private int temperature = 0;
    
    protected ShootableDoor(final Panlayer layer, final int x, final int y, ShootableDoorDefinition def) {
        setVisible(false);
        final TileMap tm = BotsnBoltsGame.tm;
        tm.getLayer().addActor(this);
        this.x = x;
        this.y = y;
        this.def = def;
        final Panple pos = getPosition();
        tm.savePosition(pos, x, y);
        pos.addX(8);
        if (x == 0) {
            doorX = 1;
        } else {
            doorX = x - 1;
            setMirror(true);
        }
        setDoorTiles(x, BotsnBoltsGame.doorTunnel, Tile.BEHAVIOR_SOLID, true);
        closeDoor();
    }
    
    private final int getBaseFrameIndex() {
        return isMirror() ? 4 : 0;
    }
    
    private final void setDoorTiles(final int x, final Panframe[] door, final byte behavior, final boolean bg) {
        final TileMap tm = BotsnBoltsGame.tm;
        final int base = getBaseFrameIndex();
        for (int j = 0; j < 4; j++) {
            final int index = tm.getIndex(x, y + j);
            final Panframe frm = door[base + j];
            if (bg) {
                tm.setBackground(index, frm, behavior);
            } else  {
                tm.setForeground(index, frm, behavior);
            }
        }
    }
    
    private final void closeDoor() {
        setDoorEnergyTiles(this.def.door);
    }
    
    private final void setDoorEnergyTiles(final Panframe[] door) {
        setDoorTiles(doorX, door, Tile.BEHAVIOR_OPEN, false);
    }
    
    private final void openDoor() {
        final TileMap tm = BotsnBoltsGame.tm;
        final int base = getBaseFrameIndex();
        final Panframe[] opening = def.opening[0];
        for (int j = 0; j < 4; j++) {
            final int yj = y + j, basej = base + j;
            tm.setBackground(x, yj, BotsnBoltsGame.doorTunnelOverlay[basej], Tile.BEHAVIOR_OPEN);
            tm.setForeground(doorX, yj, opening[basej]);
        }
        addOpenTimer(1);
        destroy();
    }
    
    private final void addOpenTimer(final int nextIndex) {
        Pangine.getEngine().addTimer(BotsnBoltsGame.tm, 1, new TimerListener() {
            @Override public final void onTimer(final TimerEvent event) {
                if (nextIndex >= def.opening.length) {
                    for (int j = 0; j < 4; j++) {
                        BotsnBoltsGame.tm.setForeground(doorX, y + j, null);
                    }
                } else {
                    setDoorEnergyTiles(def.opening[nextIndex]);
                    addOpenTimer(nextIndex + 1);
                }
            }});
    }
    
    @Override
    public final void onStep(final StepEvent event) {
        if (def.prev != null) {
            temperature--;
            if (temperature < def.prev.nextTemperature) {
                setDefinition(def.prev);
            }
        }
    }
    
    @Override
    public final void onCollision(final CollisionEvent event) {
        final Collidable collider = event.getCollider();
        if (collider.getClass() == Projectile.class) {
            temperature += 4;
            if (temperature >= def.nextTemperature) {
                if (def.next == null) {
                    openDoor();
                    ((Projectile) collider).burst();
                    collider.destroy();
                } else {
                    setDefinition(def.next);
                }
            }
        }
    }
    
    private final void setDefinition(final ShootableDoorDefinition def) {
        this.def = def;
        closeDoor();
    }
    
    @Override
    public final Pansplay getCurrentDisplay() {
        return display;
    }
    
    private final static class DoorDisplay implements Pansplay {
        @Override
        public final Panple getOrigin() {
            return FinPanple.ORIGIN;
        }
    
        @Override
        public final Panple getBoundingMinimum() {
            return min;
        }
    
        @Override
        public final Panple getBoundingMaximum() {
            return max;
        }
    }
    
    protected final static class ShootableDoorDefinition {
        private final Panframe[] door;
        private final Panframe[][] opening;
        private final ShootableDoorDefinition next;
        private final int nextTemperature;
        private ShootableDoorDefinition prev = null;
        
        protected ShootableDoorDefinition(final Panframe[] door, final Panframe[][] opening, final ShootableDoorDefinition next, final int nextTemperature) {
            this.door = door;
            this.opening = opening;
            this.next = next;
            this.nextTemperature = nextTemperature;
            if (next != null) {
                next.prev = this;
            }
        }
    }
}
