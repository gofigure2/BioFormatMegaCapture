//
// LinkedPlugin.java
//

/*
Multiple instance chainable plugin framework.

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

package loci.multiinstanceplugin;

import java.util.Set;

import loci.plugin.ImageWrapper;
import loci.plugin.annotations.Input;
import loci.plugin.annotations.Output;

/**
 * The LinkedPlugin represents a given plugin class in a given stage of a pipeline.
 *
 * It listens for the set of input images that is required and launches instances
 * of the plugin once that set is acquired.
 *
 * @author Aivar Grislis
 */
public class LinkedPlugin implements ILinkedPlugin {
    PluginAnnotations m_annotations;
    IPluginLauncher m_launcher;

    /**
     * Create an instance for a given class name.
     *
     * @param className
     */
    LinkedPlugin(String className) throws PluginClassException {

        // get associated class
        Class pluginClass = null;
        try {
            pluginClass = Class.forName(className);
        }
        catch (ClassNotFoundException e) {
            // class cannot be located
            System.out.println("Can't find " + className);
        }
        catch (ExceptionInInitializerError e) {
            // initialization provoked by this method fails
            System.out.println("Error initializing " + className + " " + e.getStackTrace());
        }
        catch (LinkageError e) {
            // linkage fails
            System.out.println("Linkage error " + className + " " + e.getStackTrace());
        }

        // validate class
        boolean success = false;
        if (null != pluginClass) {
            success = true;
            
            if (!pluginClass.isAssignableFrom(AbstractPlugin.class)) {
                success = false;
                System.out.println("Plugin should extend AbstractPlugin");
            }

            if (!pluginClass.isAssignableFrom(IPlugin.class)) {
                success = false;
                System.out.println("Plugin should implement IPlugin");
            }
        }
        
        if (success) {
            init(pluginClass);
        }
        else {
            throw new PluginClassException("Plugin class is invalid " + className);
        }
    }

    /**
     * Create an instance for a given class.
     *
     * @param className
     */
    public LinkedPlugin(Class pluginClass) {
        init(pluginClass);
    }

    /**
     * Helper function for constructors.
     *
     * @param pluginClass
     */
    private void init(Class pluginClass) {
        // examine annotations
        m_annotations = new PluginAnnotations(pluginClass);

        // create launcher
        m_launcher = new PluginLauncher(pluginClass, m_annotations);
    }

    /**
     * Gets the set of annotated input names.
     *
     * @return set of names
     */
    public Set<String> getInputNames() {
        return m_annotations.getInputNames();
    }

    /**
     * Gets the set of annotated output names.
     *
     * @return set of names
     */
    public Set<String> getOutputNames() {
        return m_annotations.getOutputNames();
    }

    /**
     * Chains default output of this node to default input of next.
     *
     * @param next
     */
    public void chainNext(ILinkedPlugin next) {
        chainNext(Output.DEFAULT, next, Input.DEFAULT);
    }

    /**
     * Chains named output of this node to default input of next.
     *
     * @param outName
     * @param next
     */
    public void chainNext(String outName, ILinkedPlugin next) {
        chainNext(outName, next, Input.DEFAULT);
    }

    /**
     * Chains default output of this node to named input of next.
     *
     * @param next
     * @param inName
     */
    public void chainNext(ILinkedPlugin next, String inName) {
        chainNext(Output.DEFAULT, next, inName);
    }

    /**
     * Chains named output of this node to named output of next.
     *
     * @param outName
     * @param next
     * @param inName
     */
    public void chainNext(String outName, ILinkedPlugin next, String inName) {
        m_launcher.chainNext(outName, next.getLauncher(), inName);
    }

    /**
     * Chains default input of this node to default output of previous.
     *
     * @param previous
     */
    public void chainPrevious(ILinkedPlugin previous) {
        chainPrevious(Input.DEFAULT, previous, Output.DEFAULT);
    }

    /**
     * Chains named input of this node to default output of previous.
     *
     * @param inName
     * @param previous
     */
    public void chainPrevious(String inName, ILinkedPlugin previous) {
        chainPrevious(inName, previous, Output.DEFAULT);
    }

    /**
     * Chains default input of this node to named output of previous.
     *
     * @param previous
     * @param outName
     */
    public void chainPrevious(ILinkedPlugin previous, String outName) {
        chainPrevious(Input.DEFAULT, previous, outName);
    }

    /**
     * Chains named input of this node to named output of previous.
     *
     * @param inName
     * @param previous
     * @param outName
     */
    public void chainPrevious(String inName, ILinkedPlugin previous, String outName) {
        m_launcher.chainPrevious(inName, previous.getLauncher(), outName);
    }

    /**
     * Used to put default image from outside the plugin.  An external put provides
     * image for an internal get from within this plugin.
     *
     * @param data
     */
    public void externalPut(ImageWrapper image) {
        externalPut(Input.DEFAULT, image);
    }

    /**
     * Used to put named image from outside the plugin.  Am external put provides
     * image for an internal get from within this plugin.
     *
     * @param inName
     * @param data
     */
    public void externalPut(String inName, ImageWrapper image) {
        m_launcher.externalPut(inName, image);
    }

    /**
     * Gets the plugin launcher for this linked plugin.
     *
     * @return launcher
     */
    public IPluginLauncher getLauncher() {
        return m_launcher;
    }

    /**
     * Quits processing images.
     */
    public void quit() {
        m_launcher.quit();
    }
}
