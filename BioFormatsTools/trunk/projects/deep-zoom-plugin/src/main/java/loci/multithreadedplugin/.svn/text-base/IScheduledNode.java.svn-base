/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.multithreadedplugin;

/**
 * Methods used by the NodeScheduler and AbstractBaseNode as well as those
 * available to use within the INode implementation.
 *
 * @author Aivar Grislis
 */
public interface IScheduledNode {

    /**
     * Adds a unique instance identifier to the name.
     *
     * @return unique name string
     */
    String uniqueInstance(String name);

    /**
     * For this instance, maps my outgoing data name to a full name which
     * includes the destination instance id and destination incoming name.
     *
     * @param outName
     * @param fullInName
     */
    void associate(String outName, String fullInName);
}
