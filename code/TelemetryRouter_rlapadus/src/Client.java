import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Lapa
 * Date: 26.05.2013
 * Time: 23:01
 * To change this template use File | Settings | File Templates.
 */
public class Client {
    private int port = 4444;
    private int counter = 0;
    private int instanceNumber;
    private SocketChannel channel;
    private Selector selector;
    private PayloadBuffer payloadBuffer;

    public Client(int instanceNumber) throws RouterException {
        this.instanceNumber = instanceNumber;
        this.payloadBuffer = new PayloadBuffer();
        initClient();
    }

    private void initClient() throws RouterException {
        try {
            channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.connect(new InetSocketAddress("localhost", port));

            waitWhileItConnects();
            selector = Selector.open();
            channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        } catch (IOException e) {
            throw new RouterException("Connection error");
        }
    }

    private void waitWhileItConnects() throws RouterException {
        try {
            while (!channel.finishConnect()) {
            }
        } catch (IOException e) {
            throw new RouterException("Exception in finish connection");
        }
    }

    /**
     *
     * @throws RouterException
     */
    public void listen() throws RouterException {
        for (; ; ) {

            try {
                if (selector.select() == 0)
                    continue;
            } catch (IOException e) {
            }

            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectedKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();

                if (key.isReadable()) {
                    read(key);
                } else if (key.isWritable()) {
                    write(key);
                }

                iterator.remove();

            }
        }
    }


    /**
     * Reads data from an unblocking channel.
     *
     * @param key selection key
     * @throws RouterException
     */
    private void read(SelectionKey key) throws RouterException {
        ByteBuffer buffer = ByteBuffer.allocate(20);
        SocketChannel clientChannel = (SocketChannel) key.channel();
        int bytesRead;

        try {
            if ((bytesRead = clientChannel.read(buffer)) > 0) {
                buffer.flip();
                CharBuffer payloadPart = Charset.defaultCharset().decode(buffer);
                payloadBuffer.addPayloadPart(payloadPart);

                if (payloadBuffer.isPayloadMessageComplete()) {
                    String completePayloadMessage = payloadBuffer.getCompletePayloadMessage();
                    System.out.println(completePayloadMessage);
                }
                buffer.clear();
            }

            if (bytesRead < 0) {
                clientChannel.close();
            }
        } catch (IOException e) {
            throw new RouterException("Can't read from client");
        }
    }

    /**
     * Writes data to an unblocking channel.
     *
     * @param key selection key
     * @throws RouterException
     */
    private void write(SelectionKey key) throws RouterException {
        if (counter < 5) {
            counter++;
            Payload payload = new Payload.Builder(instanceNumber, instanceNumber, instanceNumber)
                            .heading(instanceNumber).speed(instanceNumber).build();
            CharBuffer buffer = CharBuffer.wrap(payload.serialize());
            SocketChannel clientSocketChannel = (SocketChannel) key.channel();
            while (buffer.hasRemaining()) {
                try {
                    clientSocketChannel.write(Charset.defaultCharset().encode(buffer));
                } catch (IOException e) {
                    throw new RouterException("Can't write to client");
                }
            }
            buffer.clear();
        }
    }

    public static void main(String[] args) throws RouterException {
        Client client = new Client(Integer.decode(args[0]));
        client.listen();
    }
}
