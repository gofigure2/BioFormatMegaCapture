/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.slim.analysis;

import ij.IJ;
import ij.ImagePlus;

import java.util.ArrayList;
import java.util.List;

import loci.slim.ui.IUserInterfacePanel.FitFunction;
import loci.slim.ui.IUserInterfacePanel.FitRegion;

import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.real.DoubleType;

import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;

/**
 *
 * @author Aivar Grislis
 */
public class SLIMAnalysis {
    public static final String NONE = "None";
    IndexItem<SLIMAnalyzer, ISLIMAnalyzer> m_plugins[];
    String m_names[];

    public SLIMAnalysis() {
        // get list of plugins and their names
        List<String> names = new ArrayList<String>();
        List<IndexItem> plugins = new ArrayList<IndexItem>();
        names.add(NONE);
        plugins.add(null);

        // look for matches
        for (final IndexItem<SLIMAnalyzer, ISLIMAnalyzer> item :
                Index.load(SLIMAnalyzer.class, ISLIMAnalyzer.class, IJ.getClassLoader())) {
            plugins.add(item);
            names.add(item.annotation().name());
        }
        m_plugins = plugins.toArray(new IndexItem[0]);
        m_names = names.toArray(new String[0]);
    }

    public String[] getChoices() {
        return m_names;
    }

    public void doAnalysis(String name, Image<DoubleType> image, FitRegion region, FitFunction function) {
        IndexItem<SLIMAnalyzer, ISLIMAnalyzer> selectedPlugin = null;
        for (int i = 0; i < m_names.length; ++i) {
            if (name.equals(m_names[i])) {
                selectedPlugin = m_plugins[i];
            }
        }

        // run selected plugin
        if (null != selectedPlugin) {
            // create an instance
            ISLIMAnalyzer instance = null;
            try {
                instance = selectedPlugin.instance();
            }
            catch (InstantiationException e) {
                System.out.println("Error instantiating plugin " + e.getMessage());
            }

            if (null != instance) {
                instance.analyze(image, region, function);
            }
        }
        else {
            // default behavior
            System.out.println("Default behavior");
        }
    }
}
