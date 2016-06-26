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
package org.pandcorps.furguardians;

import java.util.*;

import org.pandcorps.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.Panput.*;
import org.pandcorps.pandam.event.action.*;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.pandax.tile.Tile.*;

public final class GemScreen extends MiniGameScreen {
    private final static int DIM = 16;
    private final static int SIZE = 6;
    private final static int NUM_COLORS = 4;
    private final static int TYPE_EMPTY = -1;
    private final static int TYPE_NORMAL = 0;
    private final static int TYPE_BREAK = 1;
    private static Panmage gemTiles = null;
    private static Panmage gemTiles2 = null;
    private static Panmage gemTiles3 = null;
    private static Panroom room = null;
    private static TileMap tm = null;
    private static TileMapImage[][] imgMap = null;
    private final static Cell[][] grid = new Cell[SIZE][SIZE];
    private final static List<Cell> currentSelection = new ArrayList<Cell>(2);
    private static boolean validSelection = true;
    private static int delay = 0;
    
    /*
    TODO
    Profile setting for last mini-game played; Menu should default to that.
    Statistics for biggest gem block, most tiles cleared in one move, Gem-games played.
    Initial shuffle can't allow diamonds beside matching gems or big gem blocks.
    Awards
    */
    
    @Override
    protected final void load() {
        room = initMiniZoom(96);
        addCursor(room, 20);
        initImages();
        initGrid();
    }
    
    @Override
    protected final void step() {
        if (tm == null) {
            return;
        }
        final long i = Pangine.getEngine().getClock() % FurGuardiansGame.TIME_FLASH;
        if (i > 1) {
            tm.setImageMap(gemTiles);
        } else if (i > 0) {
            tm.setImageMap(gemTiles3);
        } else {
            tm.setImageMap(gemTiles2);
        }
        if (delay > 0) {
            delay--;
            if (delay == 0) {
                fillBrokenCells();
            }
        }
        if ((!validSelection || currentSelection.size() > 0) && !isTouchActive(grid)) {
            onRelease();
        }
    }
    
    private final static void initImages() {
        if (gemTiles != null) {
            return;
        }
        final Pangine engine = Pangine.getEngine();
        gemTiles = engine.createImage("gem.tiles", FurGuardiansGame.RES + "misc/GemTiles.png");
        gemTiles2 = engine.createImage("gem.tiles.2", FurGuardiansGame.RES + "misc/GemTiles2.png");
        gemTiles3 = engine.createImage("gem.tiles.3", FurGuardiansGame.RES + "misc/GemTiles3.png");
    }
    
    private final static void initGrid() {
        tm = new TileMap(Pantil.vmid(), SIZE, SIZE, DIM, DIM);
        imgMap = tm.splitImageMap(gemTiles);
        tm.setForegroundDepth(10);
        room.addActor(tm);
        buildGrid();
        currentSelection.clear();
        validSelection = true;
        delay = 0;
    }
    
    private final static TileMapImage getImage(final int color, final int type) {
        final int row = color/ 2, col = color % 2, rowOff, colOff = 0;
        rowOff = (type == TYPE_NORMAL) ? 0 : 1;
        return imgMap[row * 3 + rowOff][col * 4 + colOff];
    }
    
    private final static void buildGrid() {
        final int area = SIZE * SIZE, perColor = area / NUM_COLORS;
        final int[] list = new int[area];
        for (int color = 0; color < NUM_COLORS; color++) {
            final int row = color * perColor;
            for (int i = 0; i < perColor; i++) {
                list[row + i] = color;
            }
        }
        while (true) {
            Mathtil.shuffle(list);
            if (findBigBlocks(list)) {
                continue;
            } else if (!initBreaks(list)) {
                continue;
            }
            break;
        }
        int i = 0, j = 0;
        for (int color : list) {
            int type = TYPE_NORMAL;
            if (color >= NUM_COLORS) {
                color -= NUM_COLORS;
                type = TYPE_BREAK;
            }
            grid[j][i] = new Cell(i, j, color, type);
            i++;
            if (i >= SIZE) {
                i = 0;
                j++;
            }
        }
    }
    
    private final static boolean findBigBlocks(final int[] list) {
        return false; //TODO
    }
    
    private final static boolean initBreaks(final int[] list) {
        final int size = list.length, breaks[] = new int[NUM_COLORS];
        for (int color = 0; color < NUM_COLORS; color++) {
            //TODO Start at a random spot in the list
            for (int i = 0; i < size; i++) {
                if (list[i] == color) {
                    //TODO Check neighbors
                    breaks[color] = i;
                    break;
                }
            }
        }
        for (final int i : breaks) {
            list[i] += NUM_COLORS;
        }
        return true; //TODO return false if can't find a safe spot for a break of any color
    }
    
    private final static void onRelease() {
        if (delay > 0) {
            return;
        }
        final int size = currentSelection.size();
        if (validSelection && size < 2) {
            return;
        } else if (size == 2) {
            swap();
        }
        clearCurrentSelection();
        validSelection = true;
    }
    
    private final static void swap() {
        currentSelection.get(0).swap(currentSelection.get(1));
    }
    
    private final static void fillBrokenCells() {
        //TODO
    }
    
    private final static void clearCurrentSelection() {
        for (final Cell cell : currentSelection) {
            cell.setForeground(null);
        }
        currentSelection.clear();
    }
    
    private final static class Cell implements ButtonWrapper {
        private final int i;
        private final int j;
        private final TouchButton button;
        private int color;
        private int type;
        
        private Cell(final int i, final int j, final int color, final int type) {
            this.i = i;
            this.j = j;
            setBackground(getImage(color, type));
            button = newButton();
            this.color = color;
            this.type = type;
        }
        
        private final TouchButton newButton() {
            final Pangine engine = Pangine.getEngine();
            final int x = i * DIM;
            final int y = j * DIM;
            final TouchButton button = new TouchButton(engine.getInteraction(), "Gem." + i + "." + j, x, y, DIM, DIM);
            engine.registerTouchButton(button);
            tm.register(button, new ActionStartListener() {
                @Override public final void onActionStart(final ActionStartEvent event) {
                    if (delay > 0) {
                        return;
                    } else if (!validSelection) {
                        return;
                    } else if (currentSelection.contains(Cell.this)) {
                        return;
                    }
                    final int size = currentSelection.size();
                    if (size >= 2) {
                        clearCurrentSelection();
                        validSelection = false;
                        return;
                    } else if (size > 0 && !isAdjacentTo(currentSelection.get(size - 1))) {
                        return;
                    }
                    setForeground(imgMap[6][4]);
                    currentSelection.add(Cell.this);
                }});
            return button;
        }
        
        private final Tile getTile() {
            return tm.getTile(i, j);
        }
        
        private final void setForeground(final TileMapImage img) {
            tm.setForeground(i, j, img);
        }
        
        private final Object getBackground() {
            return DynamicTileMap.getRawBackground(getTile());
        }
        
        private final void setBackground(final Object img) {
            tm.setBackground(i, j, img);
        }
        
        private final boolean isAdjacentTo(final Cell cell) {
            return GemScreen.isAdjacentTo(j, i, cell.j, cell.i);
        }
        
        private final Cell getNeighbor(final int ioff, final int joff) {
            final int ni = i + ioff, nj = j + joff;
            if (isBad(ni) || isBad(nj)) {
                return null;
            }
            return grid[nj][ni];
        }
        
        private final boolean isBad(final int i) {
            return i < 0 || i >= SIZE;
        }
        
        private final boolean swap(final Cell cell) {
            final int color = this.color;
            if (color == cell.color) {
                return false;
            }
            final Object bg = getBackground();
            final int type = this.type;
            setBackground(cell.getBackground());
            this.color = cell.color;
            this.type = cell.type;
            cell.setBackground(bg);
            cell.color = color;
            cell.type = type;
            handleComposite();
            cell.handleComposite();
            startBreak();
            cell.startBreak();
            //TODO Spark?
            return true;
        }
        
        private final void handleComposite() {
            if (type == TYPE_BREAK) {
                return;
            }
            //TODO
        }
        
        private final void startBreak() {
            if (type != TYPE_BREAK) {
                startBreakNeighbors();
                return;
            }
            continueBreak(color);
        }
        
        private final void continueBreak(final int color) {
            if (this.color != color) {
                return;
            }
            breakCell();
            continueBreakNeighbor(-1, 0, color);
            continueBreakNeighbor(1, 0, color);
            continueBreakNeighbor(0, -1, color);
            continueBreakNeighbor(0, 1, color);
        }
        
        private final void continueBreakNeighbor(final int ioff, final int joff, final int color) {
            final Cell n = getNeighbor(ioff, joff);
            if (n == null) {
                return;
            }
            n.continueBreak(color);
        }
        
        private final void startBreakNeighbors() {
            startBreakNeighbor(-1, 0);
            startBreakNeighbor(1, 0);
            startBreakNeighbor(0, -1);
            startBreakNeighbor(0, 1);
        }
        
        private final void startBreakNeighbor(final int ioff, final int joff) {
            final Cell n = getNeighbor(ioff, joff);
            if (n == null) {
                return;
            } else if (n.color != color) {
                return;
            }
            n.startBreak();
        }
        
        private final void breakCell() {
            if (type == TYPE_BREAK) {
                return;
            }
            color = -1;
            type = TYPE_EMPTY;
            setBackground(null);
            delay = 30;
            //TODO Spark?
        }
        
        @Override
        public final TouchButton getButton() {
            return button;
        }
    }
}
