import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Lapa
 * Date: 26.05.2013
 * Time: 22:33
 * To change this template use File | Settings | File Templates.
 */
public class Router {
    private final int DEFAULT_PORT = 4444;
    private final int port;
    private ServerSocketChannel channel;
    private Selector selector;
    private Map<SelectionKey, PayloadBuffer> payloadBufferMap;
    private Map<SelectionKey, List<String>> completeMessageMap;
    private List<SelectionKey> selectionKeyList;

    public Router(int port) {
        this.port = port;
    }

    public Router() {
        port = DEFAULT_PORT;
    }

    /**
     * Create a new server socket channel and binds the channel to an address.
     * The channel starts listening to incoming connections.
     * create a selector that will by used for multiplexing.
     * The selector registers the socket server channel as
     * well as all socket channels that are created.
     * The OP_ACCEPT option marks a selection key as ready
     * when the channel accepts a new connection.
     */
    private void initRouter() {
        try {
            channel = ServerSocketChannel.open();
            channel.bind(new InetSocketAddress("localhost", port));
            channel.configureBlocking(false);
            selector = Selector.open();
            channel.register(selector, SelectionKey.OP_ACCEPT);
            selectionKeyList = new ArrayList<SelectionKey>();
            payloadBufferMap = new HashMap<SelectionKey, PayloadBuffer>();
            completeMessageMap = new HashMap<SelectionKey, List<String>>();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Accept the new connection on the server socket. Since the
     * server socket channel is marked as non blocking
     * this channel will return null if no client is connected.
     *
     * @param key selection key
     */
    private void acceptNewConnection(SelectionKey key) throws RouterException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientSocketChannel;

        try {
            clientSocketChannel = serverSocketChannel.accept();
            if (clientSocketChannel != null) {
                clientSocketChannel.configureBlocking(false);
                SelectionKey clientKey = clientSocketChannel.register(
                        selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                selectionKeyList.add(clientKey);
                payloadBufferMap.put(clientKey, new PayloadBuffer());
                completeMessageMap.put(clientKey, new ArrayList<String>());
            }
        } catch (IOException e) {
            throw new RouterException("Can't accept client");
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
        PayloadBuffer payloadBuffer = payloadBufferMap.get(key);
        int bytesRead;

        try {
            if ((bytesRead = clientChannel.read(buffer)) > 0) {
                buffer.flip();
                CharBuffer payloadPart = Charset.defaultCharset().decode(buffer);
                payloadBuffer.addPayloadPart(payloadPart);

                if (payloadBuffer.isPayloadMessageComplete()) {
                    String completePayloadMessage = payloadBuffer.getCompletePayloadMessage();
                    System.out.println(completePayloadMessage);
                    putCompleteMessageInMap(key, completePayloadMessage);
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
     * Adds complete messages in every client list except the sender
     *
     * @param selectionKeySender selection key of the sender
     * @param completePayload    complete payload
     */
    private void putCompleteMessageInMap(SelectionKey selectionKeySender, String completePayload) {
        for (SelectionKey selectionKey : selectionKeyList) {
            if (selectionKey != selectionKeySender) {
                List<String> completePayloadList = completeMessageMap.get(selectionKey);
                completePayloadList.add(completePayload);
                completeMessageMap.put(selectionKey, completePayloadList);
            }
        }
    }

    /**
     * Writes data to an unblocking channel.
     *
     * @param key selection key
     * @throws RouterException
     */
    private void write(SelectionKey key) throws RouterException {
        if (existsNextPayloadMessage(key)) {
            String nextPayloadMessage = getNextPayloadMessage(key);
            CharBuffer buffer = CharBuffer.wrap(nextPayloadMessage);
            System.out.println("Sending message " + nextPayloadMessage);
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

    /**
     * Gets the next complete message for a selection key
     *
     * @param selectionKey selection key
     * @return next payload message
     */
    private String getNextPayloadMessage(SelectionKey selectionKey) {
        List<String> completePayloadMessagesList = completeMessageMap.get(selectionKey);
        String payloadMessage = completePayloadMessagesList.get(0);
        completePayloadMessagesList.remove(0);

        return payloadMessage;
    }

    /**
     * Tests if a payload message exists for a selection key
     *
     * @param selectionKey selection key
     * @return true if message exists false otherwise
     */
    private boolean existsNextPayloadMessage(SelectionKey selectionKey) {
        List<String> completeMessagePayload = completeMessageMap.get(selectionKey);

        if (completeMessagePayload == null) {
            return false;
        } else {
            if (completeMessagePayload.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    /**
     * When the socket server accepts a connection this key
     * is added to the list of selected keys of the selector.
     * When asked for the selected keys, this key is returned
     * and hence we know that a new connection has been accepted.
     * The select method is a blocking method which returns when
     * at least one of the registered channel is selected.
     * The selection key could either by the socket server informing
     * that a new connection has been made, or
     * a socket client that is ready for read/write.
     *
     * @throws RouterException
     */
    public void listen() throws RouterException {
        for (; ; ) {

            try {
                if (selector.select() == 0)
                    continue;
            } catch (IOException e) {
                throw new RouterException("Exception in select");
            }

            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectedKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();

                if (key.isAcceptable()) {
                    acceptNewConnection(key);
                } else if (key.isReadable()) {
                    read(key);
                } else if (key.isWritable()) {
                    write(key);
                }

                iterator.remove();

            }
        }
    }

    public static void main(String[] args) throws RouterException {
        Router router = new Router();
        router.initRouter();
        router.listen();
    }
}
