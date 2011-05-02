/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.workflow;

/**
 *
 * @author Aivar Grislis
 */
public class WorkFlowFactory implements IModuleFactory {
    private static WorkFlowFactory s_instance = null;

    private WorkFlowFactory() {
    }

    public static synchronized WorkFlowFactory getInstance() {
        if (null == s_instance) {
            s_instance = new WorkFlowFactory();
        }
        return s_instance;
    }

    /**
     * Creates a workflow from XML.
     *
     * @param xml
     * @return
     */
    public IWorkFlow create(String xml) {
        IWorkFlow workFlow = new WorkFlow();
        workFlow.fromXML(xml);
        return workFlow;
    }

}
