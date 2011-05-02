/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.multiinstanceplugin.deepzoom;

import loci.multiinstanceplugin.AbstractPlugin;
import loci.multiinstanceplugin.ILinkedPlugin;
import loci.multiinstanceplugin.IPlugin;
import loci.multiinstanceplugin.LinkedPlugin;
import loci.plugin.ImageWrapper;
import loci.plugin.annotations.Img;
import loci.plugin.annotations.Input;
import loci.plugin.annotations.Output;

    /**
     * Class at the top of the image chain.  Passes image on to CutTilesProcessor and also
     * ScaleInHalfProcessor.  Note that this assumes that the CutTilesProcessor handles the image
     * in a read-only fashion.
     *
     * When we get back from the inital chain to the CutTilesProcessor all of the tiles for this
     * image level will have been cut and written out.
     */
    @Input
    @Output({ @Img(LevelProcessor.TILE), @Img(LevelProcessor.HALF) })
    public class LevelProcessor extends AbstractPlugin implements IPlugin
    {
        static final String TILE = "TILE";
        static final String HALF = "HALF";

        public LevelProcessor()
        {
        }

        public void process() {
            ImageWrapper image1 = get();
            ImageWrapper image2 = new ImageWrapper(image1);

            put(TILE, image1);

            int level = ((Integer)image2.getProperties().get(DeepZoomExporter.LEVEL)).intValue();
            if (level > 0) {
                level--;
                image2.getProperties().set(DeepZoomExporter.LEVEL, new Integer(level));

                put(HALF, image2); //TODO we are assuming tile & halve are non-destructive!!!!
            }
        }
    }
