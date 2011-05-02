/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.multithreadedplugin;

/**
 * Defines call that are made externally to an INode.
 *
 * @author Aivar Grislis
 */
public interface INode extends IScheduledNode {

    /**
     * Default input/output data name if none is specified.
     */
    public static final String DEFAULT = "DEFAULT";


    /**
     * Chains default output of this node to default input of next.
     *
     * @param next
     */
    public void chainNext(IScheduledNode next);

    /**
     * Chains named output of this node to default input of next.
     *
     * @param outName
     * @param next
     */
    public void chainNext(String outName, IScheduledNode next);

    /**
     * Chains default output of this node to named input of next.
     *
     * @param next
     * @param inName
     */
    public void chainNext(IScheduledNode next, String inName);

    /**
     * Chains named output of this node to named output of next.
     *
     * @param outName
     * @param next
     * @param inName
     */
    public void chainNext(String outName, IScheduledNode next, String inName);

    /**
     * Chains default input of this node to default output of previous.
     *
     * @param previous
     */
    public void chainPrevious(IScheduledNode previous);

    /**
     * Chains named input of this node to default output of previous.
     *
     * @param inName
     * @param previous
     */
    public void chainPrevious(String inName, IScheduledNode previous);

    /**
     * Chains default input of this node to named output of previous.
     *
     * @param previous
     * @param outName
     */
    public void chainPrevious(IScheduledNode previous, String outName);

    /**
     * Chains named input of this node to named output of previous.
     *
     * @param inName
     * @param previous
     * @param outName
     */
    public void chainPrevious(String inName, IScheduledNode previous, String outName);

    /**
     * Used to put default data from outside the node.  An external put provides
     * data for an internal get.
     *
     * @param data
     */
    public void externalPut(Object data);

    /**
     * Used to put named data from outside the node.  Am external put provides
     * data for an internal get.
     *
     * @param inName
     * @param data
     */
    public void externalPut(String inName, Object data);

    /**
     * Stops this node.
     */
    public void quit();

    /**
     * The implementation of this method gets and puts data to do the
     * data processing work of the node.
     */
    void run();

    /**
     * Used within the run method.  Gets default input data.
     *
     * @return data
     */
    Object get();

    /**
     * Used within the run method.  Gets named input data.
     *
     * @param inName
     * @return data
     */
    Object get(String inName);

    /**
     * Used within the run method.  Puts default output data.
     *
     * @param data
     */
    void put(Object data);

    /**
     * Used within the run method.  Puts named output data.
     *
     * @param outName
     * @param data
     */
    void put(String outName, Object data);
}
