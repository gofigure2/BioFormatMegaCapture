//
// AbstractPipelineProcessor.java
//

/*
Framework for chaining processors together; one input and one output.

Copyright (c) 2010, UW-Madison LOCI
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

package loci.chainableplugin;

import loci.plugin.ImageWrapper;

/**
 * This is the abstract base class for a chainable processor.
 *
 * @author Aivar Grislis
 */
public abstract class AbstractPipelineProcessor implements IPipelineProcessor
{
    IProcessor m_next;

    /**
     * Chains this processor to another.  Called externally from the processor
     * before processing starts.
     *
     * @param next
     */
    public void chain(IProcessor next)
    {
        this.m_next = next;
    }

    /**
     * This is the abstract method that does the work, to be implemented in the
     * concrete class processor.
     *
     * @param imageWrapper
     * @return results code from processing
     */
    public abstract int process(ImageWrapper imageWrapper);

    /**
     * This method passes on the image to the next processor.
     *
     * @param image
     * @return results code from next processor
     */
    public int nextInChainProcess(ImageWrapper image) {
        return this.m_next.process(image);
    }
}