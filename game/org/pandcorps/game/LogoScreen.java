/*
Copyright (c) 2009-2011, Andrew M. Martin
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
package org.pandcorps.game;

import org.pandcorps.core.img.Pancolor;
import org.pandcorps.pandam.*;
import org.pandcorps.pandax.text.*;
import org.pandcorps.pandax.visual.FadeScreen;

public abstract class LogoScreen extends FadeScreen {
    private Panmage font = null;
    private Panmage icon = null;
    
    public LogoScreen() {
        super(Pancolor.WHITE, 30);
    }
    
    @Override
    protected final void start() {
        final Pangine engine = Pangine.getEngine();
        engine.setBgColor(Pancolor.WHITE);
        font = engine.createImage("PandcorpsFont", "org/pandcorps/res/img/FontGradient16.png");
        final Pantext text = new Pantext("PandcorpsLogo", new ByteFont(font), "PANDCORPS");
        text.getPosition().set(48, 88);
        final Panroom room = Pangame.getGame().getCurrentRoom();
        room.addActor(text);
        icon = engine.createImage("PandcorpsIcon", "org/pandcorps/res/img/PandcorpsIcon16.png");
        final Panctor img = new Panctor("PandcorpsImage");
        img.setView(icon);
        img.getPosition().set(192, 88);
        room.addActor(img);
    }
    
    @Override
    protected final void destroy() {
        Panmage.destroy(font);
        Panmage.destroy(icon);
    }
}
