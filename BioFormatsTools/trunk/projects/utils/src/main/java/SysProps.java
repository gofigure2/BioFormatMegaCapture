//
// SysProps.java
//

/*
A collection of simple Java utilities.

Copyright (c) 2006, UW-Madison LOCI
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

import java.util.Properties;

/**
 * Dumps all Java system properties.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.loci.wisc.edu/trac/java/browser/trunk/projects/utils/src/main/java/SysProps.java">Trac</a>,
 * <a href="http://dev.loci.wisc.edu/svn/java/trunk/projects/utils/src/main/java/SysProps.java">SVN</a></dd></dl>
 */
public class SysProps {

  public static void main(String[] args) {
    printProperties(args);
  }

  public static void printProperties(String[] filters) {
    Properties props = System.getProperties();
    for (Object key : props.keySet()) {
      String name = key.toString();
      if (filterMatch(name, filters)) {
        String value = props.get(key).toString();
        printProperty(name, value);
      }
    }
  }

  public static boolean filterMatch(String s, String[] filters) {
    for (String filter : filters) {
      if (s.indexOf(filter) < 0) return false;
    }
    return true;
  }

  public static void printProperty(String name, String value) {
    if (name.endsWith(".path")) printPathProperty(name, value);
    else System.out.println(name + " = " + value);
  }

  public static void printPathProperty(String name, String value) {
    String sep = System.getProperty("path.separator");
    String[] tokens = value.split(sep);
    System.out.println(name + " =");
    for (String token : tokens) {
      System.out.println("\t" + token);
    }
  }

}
