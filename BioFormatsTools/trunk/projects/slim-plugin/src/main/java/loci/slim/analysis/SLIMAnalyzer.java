/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.slim.analysis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.java.sezpoz.Indexable;

/**
 * Used to name ISLIMAnalyzer implementations.  These names appear
 * in the dropdown list in the UI.
 *
 * Syntax:
 *  @Name("Analyzer")
 *
 * @author Aivar Grislis
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE)
@Indexable(type=ISLIMAnalyzer.class)
public @interface SLIMAnalyzer {
    String name();
}
