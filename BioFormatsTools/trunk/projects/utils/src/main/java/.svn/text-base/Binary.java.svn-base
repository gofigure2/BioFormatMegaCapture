//
// Binary.java
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

/**
 * Attempts to convert a string of zeroes and ones
 * into readable text using a variety of encodings.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.loci.wisc.edu/trac/java/browser/trunk/projects/utils/src/main/java/Binary.java">Trac</a>,
 * <a href="http://dev.loci.wisc.edu/svn/java/trunk/projects/utils/src/main/java/Binary.java">SVN</a></dd></dl>
 */
public class Binary {

  public static void main(String[] args) throws Exception {
    if (args.length < 1) {
      System.out.println("Usage: java Binary filename.txt");
      System.exit(2);
    }
    File file = new File(args[0]);
    BufferedReader fin = new BufferedReader(new FileReader(file));
    StringBuffer sb = new StringBuffer((int) file.length());
    sb.append(fin.readLine());
    String bits = sb.toString();
    System.out.println("bits: " + bits);
    System.out.println();
    for (int len=7; len<=8; len++) {
      for (int offset=0; offset<len; offset++) {
        System.out.print("len=" + len + ", offset=" + offset + ": ");
        decode(bits, len, offset);
      }
    }
  }

  public static void decode(String bits, int len, int offset) {
    int bitlen = bits.length();
    for (int i=offset; i<=bitlen-len; i+=len) {
      String s = bits.substring(i, i + len);
      int q = Integer.parseInt(s, 2);
      if (q >= 32 && q != 127) System.out.print((char) q);
    }
    System.out.println();
  }

}
