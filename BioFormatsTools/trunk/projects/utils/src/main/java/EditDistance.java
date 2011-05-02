//
// EditDistance.java
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

/**
 * Computes minimum edit distance between two strings.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.loci.wisc.edu/trac/java/browser/trunk/projects/utils/src/main/java/EditDistance.java">Trac</a>,
 * <a href="http://dev.loci.wisc.edu/svn/java/trunk/projects/utils/src/main/java/EditDistance.java">SVN</a></dd></dl>
 */
public class EditDistance {

  public static void main(String[] args) throws IOException {
    String a = args.length > 0 ? args[0] : getString("First string? ");
    String b = args.length > 1 ? args[1] : getString("Second string? ");
    System.out.println("Distance = " + getEditDistance(a, b));
  }

  public static int getEditDistance(final String s, final String t) {
    // Leveshtein algorithm
    final char[] a = s == null ? new char[0] : s.toCharArray();
    final char[] b = t == null ? new char[0] : t.toCharArray();
    final int[] w = new int[b.length + 1];

    int cur = 0, next = 0;

    for (int i=0; i<a.length; i++) {
      cur = i + 1;

      for (int j=0; j<b.length; j++) {
        next = min(
          w[j + 1] + 1,
          cur + 1,
          w[j] + (a[i] == b[j] ? 0 : 1)
        );

        w[j] = cur;
        cur = next;
      }

      w[b.length] = next;
    }

    return next;
  }

  private static int min(int a, int b, int c) {
    if (a < b && a < c) return a;
    return (b < c ? b : c);
  }

  private static String getString(String msg) throws IOException {
    System.out.print(msg);
    return new BufferedReader(new InputStreamReader(System.in)).readLine();
  }

}
