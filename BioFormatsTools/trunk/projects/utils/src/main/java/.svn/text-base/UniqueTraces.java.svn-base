//
// UniqueTraces.java
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Reads in a bunch of stack traces from standard input,
 * filters out duplicates and outputs what's left.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.loci.wisc.edu/trac/java/browser/trunk/projects/utils/src/main/java/UniqueTraces.java">Trac</a>,
 * <a href="http://dev.loci.wisc.edu/svn/java/trunk/projects/utils/src/main/java/UniqueTraces.java">SVN</a></dd></dl>
 */
public class UniqueTraces {

  public static void main(String[] args) throws IOException {
    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    ArrayList<String> traces = new ArrayList<String>();
    StringBuffer buf = null;
    while (true) {
      String line = in.readLine();
      if (line == null) break;
      if (!line.startsWith(" ") && !line.startsWith("\t")) {
        // start of a new stack trace
        if (buf == null) buf = new StringBuffer(); // first stack trace
        else {
          // record previous stack trace
          traces.add(buf.toString());
          buf.setLength(0);
        }
        // in the middle of a stack trace
      }
      if (buf != null) {
        buf.append(line);
        buf.append("\n");
      }
    }
    if (buf != null) traces.add(buf.toString());

    String[] traceStrings = new String[traces.size()];
    traces.toArray(traceStrings);

    for (int i=0; i<traceStrings.length; i++) {
      String s = traceStrings[i];
      boolean match = false;
      for (int j=0; j<traceStrings.length; j++) {
        if (i != j && s.equals(traceStrings[j])) {
          match = true;
          break;
        }
      }
      if (!match) System.out.print(s);
    }
  }

}
