//
// LineLength.java
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
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Scans text files for lines longer than a specified length.
 * Also checks for tabs and end-of-line spaces.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.loci.wisc.edu/trac/java/browser/trunk/projects/utils/src/main/java/LineLength.java">Trac</a>,
 * <a href="http://dev.loci.wisc.edu/svn/java/trunk/projects/utils/src/main/java/LineLength.java">SVN</a></dd></dl>
 */
public class LineLength implements FileFilter {

  private static final int LENGTH = 80;
  private static final int TABWIDTH = 2;

  private static final String[] LOCI_HACKS = {
    " * <dd><a href=\"http://dev.loci.wisc.edu/trac/",
    " * <a href=\"http://dev.loci.wisc.edu/svn/java/trunk/"
  };

  public static void main(String[] args) throws IOException {
    int tabWidth = TABWIDTH;
    boolean checkTabs = true, checkEndSpaces = true;
    boolean lociHacks = false;
    ArrayList<File> fileList = new ArrayList<File>();
    for (int i=0; i<args.length; i++) {
      if (args[i].startsWith("-")) {
        // argument is a flag
        if (args[i].equals("-tabwidth")) {
          int q;
          try { q = Integer.parseInt(args[++i]); }
          catch (Exception exc) { q = -1; }
          if (q <= 0) System.out.println("Ignoring bogus tab width.");
          else tabWidth = q;
        }
        else if (args[i].equals("-notabs")) checkTabs = false;
        else if (args[i].equals("-noendspaces")) checkEndSpaces = false;
        else if (args[i].equals("-locihacks")) lociHacks = true;
        else System.out.println("Unknown flag: " + args[i]);
        continue;
      }
      // argument is a filename
      String dir;
      String crap = "." + File.separator;
      if (args[i].startsWith(crap)) args[i] = args[i].substring(crap.length());
      int slash = args[i].lastIndexOf(File.separator);
      if (slash < 0) dir = ".";
      else dir = args[i].substring(0, slash);
      File[] files = new File(dir).listFiles(new LineLength(args[i]));
      if (files != null) {
        for (int j=0; j<files.length; j++) fileList.add(strip(files[j]));
      }
    }

    char[] spaces = new char[tabWidth];
    Arrays.fill(spaces, ' ');
    String tabSpaces = new String(spaces);

    File[] files = new File[fileList.size()];
    fileList.toArray(files);
    if (files.length == 0) {
      System.out.println("No matching files found.");
      System.exit(2);
    }
    int numOk = 0;
    for (int i=0; i<files.length; i++) {
      BufferedReader fin = new BufferedReader(new FileReader(files[i]));
      String line;
      int num = 0;
      boolean okay = true;
      while (true) {
        line = fin.readLine();
        num++;
        if (line == null) break;
        boolean hasTabs = checkTabs && line.indexOf("\t") >= 0;
        line = line.replaceAll("\t", tabSpaces);
        int len = line.length();
        boolean tooLong = len > LENGTH;
        if (lociHacks) {
          for (int j=0; j<LOCI_HACKS.length; j++) {
            if (line.startsWith(LOCI_HACKS[j])) {
              tooLong = false;
              break;
            }
          }
        }
        boolean endSpace = checkEndSpaces &&
          len > 0 && line.charAt(len - 1) == ' ';
        if (!tooLong && !hasTabs && !endSpace) continue;
        if (okay) {
          okay = false;
          System.out.print(files[i].getPath() + ": ");
        }
        else System.out.print(", ");
        System.out.print(num);
        if (tooLong) System.out.print("-" + len);
        if (hasTabs) System.out.print("-T");
        if (endSpace) System.out.print("-S");
      }
      if (okay) numOk++;
      else System.out.println();
    }
    System.out.println(numOk + " of " + files.length + " files OK.");
  }

  private static File strip(File file) {
    String s = file.getPath();
    return s.startsWith("." + File.separator) ?
      new File(s.substring(1 + File.separator.length())) : file;
  }

  private String exp;
  private boolean suffix;
  private File expFile;

  public LineLength(String exp) {
    this.exp = exp.trim();
    suffix = this.exp.startsWith("*.");
    if (suffix) this.exp = this.exp.substring(1);
    else expFile = new File(this.exp);
  }

  @Override
	public boolean accept(final File pathName) {
    final File strippedName = strip(pathName);
    return suffix ?
      strippedName.getName().endsWith(exp) :
      expFile.getAbsoluteFile().equals(strippedName.getAbsoluteFile());
  }

}
