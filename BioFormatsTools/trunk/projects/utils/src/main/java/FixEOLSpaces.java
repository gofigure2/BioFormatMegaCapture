//
// FixEOLSpaces.java
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
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Scans text files for end-of-line spaces or tabs, and
 * multiple consecutive blank lines, and removes them.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.loci.wisc.edu/trac/java/browser/trunk/projects/utils/src/main/java/FixEOLSpaces.java">Trac</a>,
 * <a href="http://dev.loci.wisc.edu/svn/java/trunk/projects/utils/src/main/java/FixEOLSpaces.java">SVN</a></dd></dl>
 */
public class FixEOLSpaces {

  public static void main(String[] args) throws IOException {
    for (int i=0; i<args.length; i++) {
      File inFile = new File(args[i]);
      System.out.print(inFile + "  ");

      // check for EOL spaces and tabs
      BufferedReader in = new BufferedReader(new FileReader(inFile));
      boolean process = false;
      boolean lastBlank = false;
      while (true) {
        String line = in.readLine();
        if (line == null) break;
        if (line.endsWith(" ") || line.endsWith("\t")) {
          process = true;
          break;
        }
        boolean blank = line.trim().equals("");
        if (blank && lastBlank) {
          // found consecutive blank lines
          process = true;
          break;
        }
        lastBlank = blank;
      }
      in.close();

      if (process) {
        // remove EOL spaces and tabs
        in = new BufferedReader(new FileReader(inFile));
        File outFile = new File(args[i] + ".tmp");
        PrintWriter out = new PrintWriter(new FileWriter(outFile));
        lastBlank = false;
        while (true) {
          String line = in.readLine();
          if (line == null) break;
          if (line.endsWith(" ") || line.endsWith("\t")) {
            char[] c = line.toCharArray();
            int n;
            for (n=c.length-1; n>=0; n--) {
              if (c[n] != ' ' && c[n] != '\t') break;
            }
            line = new String(c, 0, n + 1);
          }
          boolean blank = line.equals("");
          if (!blank || !lastBlank) out.println(line);
          lastBlank = blank;
        }
        out.close();
        in.close();
        inFile.delete();
        outFile.renameTo(inFile);
        System.out.println("OK");
      }
      else System.out.println("skipped");
    }
  }

}
