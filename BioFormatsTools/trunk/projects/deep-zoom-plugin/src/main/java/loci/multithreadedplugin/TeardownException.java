/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.multithreadedplugin;

/**
 *
 * @author aivar
 */
public class TeardownException extends RuntimeException {
    public TeardownException(String message) {
        super(message);
    }
}
