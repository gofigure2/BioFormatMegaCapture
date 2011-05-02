//
// SLIMBinning.java
//

/*
SLIMPlugin for combined spectral-lifetime image analysis.

Copyright (c) 2010, UW-Madison LOCI
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the UW-Madison LOCI nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package loci.slim.binning;

import ij.IJ;

import java.util.ArrayList;
import java.util.List;

import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;

/**
 *
 * @author Aivar Grislis
 */
public class SLIMBinning <T extends RealType<T>> {
    public static final String NONE = "None";
    IndexItem<SLIMBinner, ISLIMBinner> m_plugins[];
    String m_names[];

    public SLIMBinning() {
        // get list of plugins and their names
        List<String> names = new ArrayList<String>();
        List<IndexItem> plugins = new ArrayList<IndexItem>();
        names.add(NONE);
        plugins.add(null);

        // get all matches
        for (final IndexItem<SLIMBinner, ISLIMBinner> item :
                Index.load(SLIMBinner.class, ISLIMBinner.class, IJ.getClassLoader())) {
            plugins.add(item);
            names.add(item.annotation().value());
        }
        m_plugins = plugins.toArray(new IndexItem[0]);
        m_names = names.toArray(new String[0]);
    }

    public String[] getChoices() {
        return m_names;
    }

    public Image<T> doBinning(String name, Image<T> image) {
        Image<T> returnImage = image;
        IndexItem<SLIMBinner, ISLIMBinner> selectedPlugin = null;
        for (int i = 0; i < m_names.length; ++i) {
            if (name.equals(m_names[i])) {
                selectedPlugin = m_plugins[i];
            }
        }

        // run selected plugin
        if (null != selectedPlugin) {
            // create an instance
            ISLIMBinner instance = null;
            try {
                instance = selectedPlugin.instance();
            }
            catch (InstantiationException e) {
                System.out.println("Error instantiating plugin " + e.getMessage());
            }

            if (null != instance) {
                returnImage = instance.bin(image);
            }
        }
        return returnImage;
    }
}
