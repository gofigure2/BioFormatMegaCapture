//
// RandomPass.java
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

/**
 * Generates random passwords.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.loci.wisc.edu/trac/java/browser/trunk/projects/utils/src/main/java/RandomPass.java">Trac</a>,
 * <a href="http://dev.loci.wisc.edu/svn/java/trunk/projects/utils/src/main/java/RandomPass.java">SVN</a></dd></dl>
 */
public class RandomPass {

  private static final int PASS_LEN = 8;
  private static final char MIN_CHAR = '!';
  private static final char MAX_CHAR = '~';

  public static void main(String[] args) {
    int passLen = PASS_LEN;
    boolean alphaNum = false;
    for (String arg : args) {
      if (arg.equals("-nosymbols")) {
        alphaNum = true;
      }
      else {
        // assume argument is password character length
        passLen = Integer.parseInt(arg);
      }
    }
    StringBuffer sb = new StringBuffer(passLen + 2);
    int range = MAX_CHAR - MIN_CHAR + 1;
    int i = 0;
    while (i < passLen) {
      char c = (char) (range * Math.random() + MIN_CHAR);
      if (alphaNum) {
        boolean alpha = (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
        boolean num = c >= '0' && c <= '9';
        if (!alpha && !num) continue;
      }
      sb.append(c);
      i++;
    }
    System.out.println(sb);
  }

}
