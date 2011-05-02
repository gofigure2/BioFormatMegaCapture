//
// ILinkedPlugin.java
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

/**
 * Provides an interface to chain plugins together.
 *
 * The ILinkedPlugin instance is associated with a plugin class.  It has
 * an IPluginLauncher that it uses to chain plugins.
 *
 * @author Aivar Grislis
 */
public interface ILinkedPlugin {
    /**
     * Gets the set of annotated input names.
     *
     * @return set of names
     */
    public Set<String> getInputNames();

    /**
     * Gets the set of annotated output names.
     *
     * @return set of names
     */
    public Set<String> getOutputNames();

    /**
     * Chains default output of this node to default input of next.
     *
     * @param next
     */
    public void chainNext(ILinkedPlugin next);

    /**
     * Chains named output of this node to default input of next.
     *
     * @param outName
     * @param next
     */
    public void chainNext(String outName, ILinkedPlugin next);

    /**
     * Chains default output of this node to named input of next.
     *
     * @param next
     * @param inName
     */
    public void chainNext(ILinkedPlugin next, String inName);

    /**
     * Chains named output of this node to named output of next.
     *
     * @param outName
     * @param next
     * @param inName
     */
    public void chainNext(String outName, ILinkedPlugin next, String inName);

    /**
     * Chains default input of this node to default output of previous.
     *
     * @param previous
     */
    public void chainPrevious(ILinkedPlugin previous);

    /**
     * Chains named input of this node to default output of previous.
     *
     * @param inName
     * @param previous
     */
    public void chainPrevious(String inName, ILinkedPlugin previous);

    /**
     * Chains default input of this node to named output of previous.
     *
     * @param previous
     * @param outName
     */
    public void chainPrevious(ILinkedPlugin previous, String outName);

    /**
     * Chains named input of this node to named output of previous.
     *
     * @param inName
     * @param previous
     * @param outName
     */
    public void chainPrevious(String inName, ILinkedPlugin previous, String outName);

    /**
     * Used to put default image from outside the plugin.  An external put provides
     * image for an internal get from within this plugin.
     *
     * @param image
     */
    public void externalPut(ImageWrapper image);

    /**
     * Used to put named image from outside the plugin.  Am external put provides
     * image for an internal get from within this plugin.
     *
     * @param inName
     * @param image
     */
    public void externalPut(String inName, ImageWrapper image);

    /**
     * Gets the plugin launcher for this linked plugin.
     *
     * @return launcher
     */
    public IPluginLauncher getLauncher();

    /**
     * Quits processing images.
     */
    public void quit();
}
