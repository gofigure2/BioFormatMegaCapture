/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.plugin.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 *
 * @author aivar
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Input {
    public final String DEFAULT = "INPUT";
    //Bug ID: 6954300
    // Annotation with generics causes javac to fail when annotation processor present
    // State: 3-Accepted, bug Priority: 4-Low
    // Submit Date: 20-MAY-2010
    //http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6954300
    //Img[] value() default { @Img };
    //Img[] value() default { @Img("DEFAULT") };
    Img[] value() default { };
}
