//
// DumpClass.java
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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Uses reflection to print a bunch of information about a class.
 * You can also use "javap -c" for a similar result.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.loci.wisc.edu/trac/java/browser/trunk/projects/utils/src/main/java/DumpClass.java">Trac</a>,
 * <a href="http://dev.loci.wisc.edu/svn/java/trunk/projects/utils/src/main/java/DumpClass.java">SVN</a></dd></dl>
 */
public class DumpClass {

  public static void main(String[] args) throws Exception {
    for (int i=0; i<args.length; i++) {
      Class<?> c = Class.forName(args[i]);
      System.out.println("Class = " + c);

      System.out.println();
      System.out.println("[Constructors]");
      Constructor<?>[] con = c.getDeclaredConstructors();
      for (int j=0; j<con.length; j++) System.out.println(con[j]);

      System.out.println();
      System.out.println("[Fields]");
      Field[] f = c.getDeclaredFields();
      for (int j=0; j<f.length; j++) System.out.println(f[j]);

      System.out.println();
      System.out.println("[Methods]");
      Method[] m = c.getDeclaredMethods();
      for (int j=0; j<m.length; j++) System.out.println(m[j]);
    }
  }

}
