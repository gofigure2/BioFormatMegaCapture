/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.multithreadedplugin;

import loci.plugin.annotations.Img;
import loci.plugin.annotations.Input;
import loci.plugin.annotations.Output;

import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.lang.reflect.AnnotatedElement;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This abstract base class handles interaction with the NodeScheduler.
 *
 * @author Aivar Grislis
 */
public abstract class AbstractNode extends Thread implements INode, IScheduledNode {
    private enum InputOutput { INPUT, OUTPUT };
    UUID m_id = UUID.randomUUID();
    volatile boolean m_idle = true;
    Map<String, String> m_map = new HashMap();
    Set<String> m_inputNames = getInputNamesFromAnnotations();
    Set<String> m_outputNames = getOutputNamesFromAnnotations();
    Map<String, Object> m_inputs = new HashMap();

    /**
     * Builds a name that is tied to this instance of the subclass.  Used to
     * create a unique input name.
     *
     * @param name unqualified name
     * @return qualified name
     */
    public String uniqueInstance(String name) {
        return (m_id.toString() + '-' + name);
    }

    /**
     * When chaining associates a named output with a named input tied to a
     * particular instance of chained subclass.
     *
     * @param outName
     * @param fullName
     */
    public void associate(String outName, String fullName) {
        m_map.put(outName, fullName);
    }

    /**
     * This is the body of the plugin, defined in subclass.
     */
    abstract public void process();

    /**
     * Gets the default input data from previous in chain.  Called from subclass.
     *
     * @return data
     */
    public Object get() {
        return get(INode.DEFAULT);
    }

    /**
     * Gets a named input image from previous in chain.  Called from subclass.
     *
     * @param inName
     * @return image
     */
    public Object get(String inName) {
        System.out.println("get " + inName);
        Object input = m_inputs.get(inName);
        if (null == input) {
            // run-time request disagrees with annotation
            nameNotAnnotated(InputOutput.INPUT, inName);
        }
        return input;
    }

    /**
     * Puts the default output data to next in chain (if any).  Called from subclass.
     *
     * @param data
     */
    public void put(Object data) {
        put(INode.DEFAULT, data);
    }

    /**
     * Puts named output data to next in chain (if any).  Called from subclass.
     *
     * @param outName
     * @param data
     */
    public void put(String outName, Object data) {
        System.out.println("put " + outName);
        if (isAnnotatedName(InputOutput.OUTPUT, outName)) {
            System.out.println("was annotated");
            // anyone interested in this output data?
            String fullName = m_map.get(outName);
            System.out.println("full name is " + fullName);
            if (null != fullName) {
                // yes, pass it on
                NodeScheduler.getInstance().put(fullName, data);
            }
        }
    }

    /**
     * Chains default output of this node to default input of next node.
     *
     * @param next node
     */
    public void chainNext(IScheduledNode next) {
        chainNext(INode.DEFAULT, next, INode.DEFAULT);
    }

    /**
     * Chains named output of this node to default input of next node.
     *
     * @param outName
     * @param next node
     */
    public void chainNext(String outName, IScheduledNode next) {
        chainNext(outName, next, INode.DEFAULT);
    }

    /**
     * Chains default output of this node to named input of next node.
     *
     * @param next node
     * @param inName
     */
    public void chainNext(IScheduledNode next, String inName) {
        chainNext(INode.DEFAULT, next, inName);
    }

    /**
     * Chains named output of this node to named input of next node.
     *
     * @param outName
     * @param next node
     * @param inName
     */
    public void chainNext(String outName, IScheduledNode next, String inName) {
        NodeScheduler.getInstance().chain(this, outName, next, inName);
    }

    /**
     * Chains default input of this node to default output of previous node.
     *
     * @param previous node
     */
    public void chainPrevious(IScheduledNode previous) {
        chainPrevious(INode.DEFAULT, previous, INode.DEFAULT);
    }

    /**
     * Chains named input of this node to default output of previous node.
     *
     * @param inName
     * @param previous node
     */

    public void chainPrevious(String inName, IScheduledNode previous) {
        chainPrevious(inName, previous, INode.DEFAULT);
    }

    /**
     * Chains default input of this node to named output of previous node.
     *
     * @param previous node
     * @param outName
     */
    public void chainPrevious(IScheduledNode previous, String outName) {
        chainPrevious(INode.DEFAULT, previous, outName);
    }

    /**
     * Chains named input of this node to named output of previous node.
     *
     * @param inName
     * @param previous node
     * @param outName
     */
    public void chainPrevious(String inName, IScheduledNode previous, String outName) {
        NodeScheduler.getInstance().chain(previous, outName, this, inName);
    }

    /**
     * Loops until quitting time.
     * 
     */
    @Override
    public void run() {
        System.out.println("Node " + m_id + " initiated");
        try {
            while (true) {
                System.out.println("input names " + getInputNames());
                // wait for all the input data as annotated
                for (String inputName : getInputNames()) {
                    Object data = internalGet(inputName);
                    System.out.println("got " + inputName);
                    // save data in local map
                    m_inputs.put(inputName, data);
                }
                System.out.println("now run " + m_inputs.keySet());
                // now run the subclass main routine
                m_idle = false;
                process();
                m_idle = true;

                // done with input data map
                m_inputs.clear();
            }
        }
        catch (TeardownException e) {
            System.out.println("Node " + m_id + " terminated " + (m_idle ? "" : "not ") + "idle");
            m_inputs.clear();
        }
    }

    /**
     * Signals quitting time.
     */
    public void quit() {
        NodeScheduler.getInstance().quit();
    }

    /**
     * Feeds an image to the default input of the subclass.
     *
     * @param image
     */
    public void externalPut(Object object) {
        externalPut(INode.DEFAULT, object);
    }

    /**
     * Feeds an image to a named input of the subclass.
     *
     * @param inName
     * @param data
     */
    public void externalPut(String inName, Object object) {
        if (isAnnotatedName(InputOutput.INPUT, inName)) {
            String fullInName = uniqueInstance(inName);
            NodeScheduler.getInstance().put(fullInName, object);
        }
    }

    /**
     * Gets the set of annotated input names.
     *
     * @return set of names
     */
    public Set<String> getInputNames() {
        return m_inputNames;
    }

    /**
     * Gets the set of annotated output names.
     *
     * @return set of names
     */
    public Set<String> getOutputNames() {
        return m_outputNames;
    }

    /**
     * Builds a set of input object names from the subclass annotations.
     * 
     * @param nodeClass
     * @return set of names
     */
    private Set<String> getInputNamesFromAnnotations() {
        Set<String> set = new HashSet<String>();
        Annotation annotation = this.getClass().getAnnotation(Input.class);
        if (annotation instanceof Input) {
            Input inputs = (Input) annotation;
            Img images[] = inputs.value();
            for (Img image : images) {
                set.add(image.value());
            }
        }
        return set;
    }

    /**
     * Builds a set of output image names from the subclass annotations.
     *
     * @param nodeClass
     * @return
     */
    private Set<String> getOutputNamesFromAnnotations() {
        Set<String> set = new HashSet<String>();
        Annotation annotation = this.getClass().getAnnotation(Output.class);
        if (annotation instanceof Output) {
            Output inputs = (Output) annotation;
            Img images[] = inputs.value();
            for (Img image : images) {
                set.add(image.value());
            }
        }
        return set;
    }

    /**
     * Checks whether a given name appears in the annotations for input or
     * output images.  Puts out an error message.
     *
     * @param input whether input or output
     * @param name putative input/output name
     * @return whether or not annotated
     */
    private boolean isAnnotatedName(InputOutput inOut, String name) {
        boolean returnValue = true;
        Set<String> names = (InputOutput.INPUT == inOut) ? getInputNames() : getOutputNames();
        if (!names.contains(name)) {
            nameNotAnnotated(inOut, name);
            returnValue = false;
        }
        return returnValue;
    }

    /**
     * Puts out an error message that an annotation is missing.
     *
     * @param inOut whether input or output
     * @param name
     */
    private void nameNotAnnotated(InputOutput inOut, String name) {
        System.out.println("Missing annotation: @" + ((InputOutput.INPUT == inOut) ? "In" : "Out") + "put({@Img=\"" + name + "\"})" );
    }

    /**
     * Gets an input image from the node scheduler.
     *
     * @param inName
     * @return Image
     */
    private Image internalGet(String inName) {
        String fullName = uniqueInstance(inName);
        return (Image) NodeScheduler.getInstance().get(fullName);
    }

}
