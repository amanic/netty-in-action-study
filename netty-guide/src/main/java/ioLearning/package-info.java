/**
 *
 * io：stream读一个少一个|单向
 * nio：channel中的数据读到buffer当中，再从buffer中读取到程序中|buffer是双向，使用flip方法。
 *
 *
 * java, io 中最为核心的一个概念是流（Stream），面向流的编程。Java 中，一个流要么是输入流，要么是输出流，不可能同时既是输入流又是输出流。
 *
 * java, nio 中拥有 3 个核心概念：Selector, channe1 与 Buffer。在 java, nio 中，我们是面向块（b1 ock）或是缓冲区（buffer）编程的。
 * Buffer 本身就是一块内存，底层
 *
 * 除了数组之外，Buffer 还提供了对于数据的结构化访问方式，并且可以追踪到系统的读写过程。
 *
 * Java 中的 7 种原生数据类型都有各自对应的 offer 类型，如 Intbuffer, Longbuffer, Bytebuffer 及 Charbuffer 等等。
 *
 * Channe 指的是可以向其写入数据或是从中读取数据的对象，它类似于 java, io 中的 Stream
 *
 * 所有数据的读写都是通过 Buffer 来进行的，永远不会出现直接向 Channel 写入数据的情况，或是直接从 Channel 读取数据的情况。
 *
 * 与 Streame 不同的是，Channel 是双向的，一个流只可能是 nputstream 或是 Outputstream, Channe 打开后则可以进行读取、写入或是读写。
 * 由于 Channel 是双向的，因此它能更好地反映出底层操作系统的真实情况；在 inux 系统中，底层操作系统的通道就是双向的
 *
 */



package ioLearning;