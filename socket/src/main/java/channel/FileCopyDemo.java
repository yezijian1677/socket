package channel;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author augenye
 * @date 2019/11/14 8:06 下午
 */
interface FileCopyRunner {
    void copyFile(File source, File target);
}

public class FileCopyDemo {

    private static final int ROUNDS = 5;
    private static void benchmark(FileCopyRunner test, File source, File target) {
        long elapsed = 0L;
        for (int i = 0; i < ROUNDS; i++) {
            long startTime = System.currentTimeMillis();
            test.copyFile(source, target);
            elapsed += System.currentTimeMillis() - startTime;
        }
        elapsed = elapsed / ROUNDS;

        System.out.println(test.getClass().getName() + "的运行时间:" + elapsed);

    }

    private static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {

        FileCopyRunner noBufferStreamCopy = new FileCopyRunner() {
            @Override
            public void copyFile(File source, File target) {
                InputStream fin = null;
                OutputStream fout = null;
                try {
                    fin = new FileInputStream(source);
                    fout = new FileOutputStream(target);

                    //当read = -1 的时候表示读到末尾，每次read一次就得到一个字节
                    int result;
                    while ((result = fin.read()) != -1) {
                        //读出后输出
                        fout.write(result);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    close(fin);
                    close(fout);
                }

            }
        };

        FileCopyRunner bufferedStreamCopy = new FileCopyRunner() {
            @Override
            public void copyFile(File source, File target) {
                InputStream fin = null;
                OutputStream fout = null;

                try {
                    fin = new BufferedInputStream(new FileInputStream(source));
                    fout = new BufferedOutputStream(new FileOutputStream(target));

                    //定义一个缓冲区大小
                    byte[] buffer = new byte[1024];

                    int result;
                    while ((result = fin.read(buffer)) != -1) {
                        fout.write(buffer, 0, buffer.length);
                        // 缓冲区、偏移量（从缓冲区的哪个位置开始读）、读的数据的长度
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    close(fin);
                    close(fout);
                }
            }
        };

        FileCopyRunner nioBufferCopy = new FileCopyRunner() {
            @Override
            public void copyFile(File source, File target) {
                //申明channel
                FileChannel fin = null;
                FileChannel fout = null;
                //通过对应的文件输入流和文件输出流 获取文件通道
                try {
                    fin = new FileInputStream(source).getChannel();
                    fout = new FileOutputStream(target).getChannel();

                    //创建一个buffer并且分配空间, 使用静态方法 allocate
                    ByteBuffer buffer = ByteBuffer.allocate(1024);

                    int result;
                    while ((result = fin.read(buffer)) != -1) {
                        buffer.flip();
                        //hasRemaining 是否还有数据未读
                        while (buffer.hasRemaining()) {
                            fout.write(buffer);
                        }
                        //重置为初始位置
                        buffer.clear();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    close(fin);
                    close(fout);
                }
            }
        };

        FileCopyRunner nioTransferCopy = new FileCopyRunner() {
            @Override
            public void copyFile(File source, File target) {
                FileChannel fin = null;
                FileChannel fout = null;

                try {
                    fin = new FileInputStream(source).getChannel();
                    fout = new FileOutputStream(target).getChannel();

                    long transfered = 0L;
                    long size = fin.size();
                    while (transfered != size) {
                        transfered += fin.transferTo(0, size, fout);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    close(fin);
                    close(fout);
                }
            }
        };

        File fileSource = new File("/Users/yezijian/Downloads/test/1.json");
        File fileTarget = new File("/Users/yezijian/Downloads/test/1copy.json");

        System.out.println("------- Copying file ----------");
        benchmark(noBufferStreamCopy, fileSource, fileTarget);
        benchmark(bufferedStreamCopy, fileSource, fileTarget);
        benchmark(nioBufferCopy, fileSource, fileTarget);
        benchmark(nioTransferCopy, fileSource, fileTarget);
        System.out.println("------- End ----------");

    }

}
