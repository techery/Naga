/*
Copyright (c) 2008-2011 Christoffer Lernö

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package naga.packetreader;

/**
 * Reads a bytestream delimited by '\n'.
 * <p>
 * This can be used for reading lines of ASCII characters.
 * 
 * @author Christoffer Lerno
 */
public class AsciiLinePacketReader extends DelimiterPacketReader
{
	/**
	 * Creates a '\n' delimited reader with an unlimited max buffer size.
	 */
	public AsciiLinePacketReader()
	{
		super((byte) '\n');
	}

	/**
	 * Creates a '\n' delimited reader with the given max line length
	 * and default read buffer size.
	 * <p>
	 * Exceeding the line length will throw an IOException.
	 *
	 * @param maxLineLength maximum line length.
	 */
	public AsciiLinePacketReader(int maxLineLength)
	{
		super((byte) '\n', maxLineLength);
	}

}
