//
// FileOpsTest.java
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

import java.io.File;

/**
 * Tests performance of File.exists() vs File.listFiles().
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.loci.wisc.edu/trac/java/browser/trunk/projects/utils/src/main/java/FileOpsTest.java">Trac</a>,
 * <a href="http://dev.loci.wisc.edu/svn/java/trunk/projects/utils/src/main/java/FileOpsTest.java">SVN</a></dd></dl>
 */
public class FileOpsTest {
  public static void main(String[] args) {
    int iter = 1000;
    if (args.length > 0) iter = Integer.parseInt(args[0]);
    long t1, t2;

    File dot = new File(".");
    File[] list = dot.listFiles();

    // time File.exists with an existing file
    t1 = System.currentTimeMillis();
    for (int i=0; i<iter; i++) list[i % list.length].exists();
    t2 = System.currentTimeMillis();
    System.out.println("File.exists() for existing = " + (t2 - t1) + " ms");

    // time File.exists with a non-existent file
    File non = new File("this-file-is-unlikely-to-exist");
    t1 = System.currentTimeMillis();
    for (int i=0; i<iter; i++) non.exists();
    t2 = System.currentTimeMillis();
    System.out.println("File.exists() for non-exist = " + (t2 - t1) + " ms");

    // time File.listFiles
    t1 = System.currentTimeMillis();
    for (int i=0; i<iter; i++) dot.listFiles();
    t2 = System.currentTimeMillis();
    System.out.println("File.listFiles() = " + (t2 - t1) +
      " ms (" + ((t2 - t1) / list.length) + "/file)");
  }
}

/*
Some results on a MacPro 2 x 2.66 GHz Dual-Core Intel Xeon
Networked file system over SMB on local network (gigabit?)
Windows and Linux results via Parallels v2

LINUX - NETWORK
File.exists() for existing = 16 ms
File.exists() for non-exist = 12 ms
File.listFiles() = 1755 ms (2/file)

MAC OS X - NETWORK
curtis@monk:~/data/perkinelmer/koen/20061107165034$ java FileOpsTest
File.exists() for existing = 58 ms
File.exists() for non-exist = 602 ms
File.listFiles() = 4519 ms (6/file)

WIN XP - NETWORK
Z:/perkinelmer/koen/20061107165034>java -cp C:/svn/java/utils FileOpsTest
File.exists() for existing = 2043 ms
File.exists() for non-exist = 1763 ms
File.listFiles() = 40882 ms (60/file)

LINUX - LOCAL
File.exists() for existing = 10 ms
File.exists() for non-exist = 17 ms
File.listFiles() = 71 ms (2/file)

MAC OS X - LOCAL
curtis@monk:~/svn/java/jar$ java FileOpsTest
File.exists() for existing = 7 ms
File.exists() for non-exist = 12 ms
File.listFiles() = 149 ms (5/file)

WIN XP - LOCAL
C:/svn/java/jar>java -cp ../utils FileOpsTest
File.exists() for existing = 0 ms
File.exists() for non-exist = 0 ms
File.listFiles() = 91 ms (3/file)
*/
