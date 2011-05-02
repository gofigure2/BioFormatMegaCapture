/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.slim;

/**
 *
 * @author aivar
 */
public class Excitation {
    private final String m_fileName;
    private final float[] m_values;
    private int m_start;
    private int m_stop;
    private float m_base;

    public Excitation(String fileName, float[] values) {
        m_fileName = fileName;
        m_values = values;
        float[] cursors = CursorHelper.estimateExcitationCursors(values);
        System.out.println("start " + cursors[0] + " stop " + cursors[1] + " base " + cursors[2]);
        m_start = (int) cursors[0];
        m_stop  = (int) cursors[1];
        m_base  = cursors[2];
    }

    public String getFileName() {
        return m_fileName;
    }

    public float[] getValues() {
        return m_values;
    }

    public int getStart() {
        return m_start;
    }

    public int getStop() {
        return m_stop;
    }

    public float getBase() {
        return m_base;
    }

}
